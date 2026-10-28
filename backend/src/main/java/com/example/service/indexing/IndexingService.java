package com.example.service.indexing;

import com.example.entity.CodeChunks;
import com.example.entity.IndexStatus;
import com.example.entity.RepositoryEntity;
import com.example.entity.User;
import com.example.repository.CodeChunkRepo;
import com.example.repository.RepositoryRepo;
import com.example.service.UserService;
import com.example.service.github.GithubApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class IndexingService {

    private final RepositoryRepo repositoryRepo;
    private final CodeChunkRepo codeChunkRepo;
    private final UserService userService;
    private final GithubApiClient githubApiClient;
    private final CodeFileFilter codeFileFilter;
    private final CodeChunker codeChunker;
    private final EmbeddingService embeddingService;

    /** Flush chunks to DB in batches (avoids N+1 individual saves) */
    private static final int DB_BATCH_SIZE = 50;

    /** Update progress in DB every N processed files (reduces DB writes during indexing) */
    private static final int PROGRESS_UPDATE_INTERVAL = 5;

    @Async("indexingExecutor")
    public void indexRepository(UUID repositoryId) {

        RepositoryEntity repository = repositoryRepo.findById(repositoryId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Repository not found: " + repositoryId
                        )
                );

        try {

            // 1. Mark indexing started
            repository.setIndexStatus(IndexStatus.INDEXING);
            repository.setErrorMsg(null);
            repository.setFileProcessed(0);
            repository.setChunkCount(0);

            repositoryRepo.save(repository);

            // 2. Get GitHub user
            User user = userService.getById(repository.getUserId());

            // 3. Get decrypted GitHub token
            String accessToken = userService.decryptAccessToken(user);

            // 4. Delete old chunks
            codeChunkRepo.deleteByRepositoryId(repositoryId);

            // 5. Get all repository files using Git Trees API (single HTTP request instead of recursive)
            List<Map<String, Object>> files =
                    githubApiClient.getTreeFiles(
                            accessToken,
                            repository.getOwner(),
                            repository.getName(),
                            repository.getDefaultBranch()
                    );

            log.info("Repository {}: found {} total files via git tree", repository.getName(), files.size());

            repository.setTotalFiles(files.size());
            repositoryRepo.save(repository);

            int processedFiles = 0;
            int totalChunks = 0;

            // Batch buffer — collect chunks and flush in bulk to avoid N+1 DB saves
            List<CodeChunks> chunkBatch = new ArrayList<>();

            // 6. Process every file
            for (Map<String, Object> file : files) {

                String path = String.valueOf(file.get("path"));

                // 7. Filter out non-code files
                if (!codeFileFilter.shouldIndex(path)) {
                    continue;
                }

                try {

                    // 8. Fetch actual file content from GitHub
                    Map<String, Object> fileData =
                            githubApiClient.getFile(
                                    accessToken,
                                    repository.getOwner(),
                                    repository.getName(),
                                    path
                            );

                    String content = decodeGithubContent(fileData);

                    if (content.isBlank()) {
                        continue;
                    }

                    // 9. Detect language from extension
                    String language = detectLanguage(path);

                    // 10. Split content into chunks
                    List<String> chunks = codeChunker.chunk(content);

                    if (chunks.isEmpty()) {
                        continue;
                    }

                    // 11. Generate embeddings for ALL chunks of this file in one batch call
                    List<float[]> embeddings = embeddingService.generateEmbeddings(chunks);

                    // 12. Build chunk entities from the batch embeddings
                    String metadata = createMetadata(repository, path, language);
                    for (int i = 0; i < chunks.size(); i++) {
                        chunkBatch.add(
                                CodeChunks.builder()
                                        .repositoryId(repositoryId)
                                        .filePath(path)
                                        .chunkIndex(i)
                                        .content(chunks.get(i))
                                        .language(language)
                                        .metadata(metadata)
                                        .embedding(Arrays.toString(embeddings.get(i)))
                                        .build()
                        );
                        totalChunks++;
                    }

                    processedFiles++;

                    // 13. Flush chunk batch to DB when threshold reached
                    if (chunkBatch.size() >= DB_BATCH_SIZE) {
                        codeChunkRepo.saveAll(chunkBatch);
                        chunkBatch.clear();
                    }

                    // 14. Persist progress every N files (avoids a DB write per file)
                    if (processedFiles % PROGRESS_UPDATE_INTERVAL == 0) {
                        repository.setFileProcessed(processedFiles);
                        repository.setChunkCount(totalChunks);
                        repositoryRepo.save(repository);
                    }

                } catch (Exception fileError) {
                    log.warn("Skipping file {} in {}: {}", path, repository.getName(), fileError.getMessage());
                }
            }

            // 15. Flush any remaining chunks that didn't fill the batch
            if (!chunkBatch.isEmpty()) {
                codeChunkRepo.saveAll(chunkBatch);
            }

            // 16. Mark repository as ready
            repository.setIndexStatus(IndexStatus.READY);
            repository.setFileProcessed(processedFiles);
            repository.setChunkCount(totalChunks);

            repositoryRepo.save(repository);

            log.info("Indexing complete for {}. Files indexed: {}, Chunks: {}",
                    repository.getName(), processedFiles, totalChunks);

        } catch (Exception e) {

            log.error("Indexing failed for repository {}: {}", repositoryId, e.getMessage(), e);

            repository.setIndexStatus(IndexStatus.FAILED);
            repository.setErrorMsg(e.getMessage());

            repositoryRepo.save(repository);

            throw new RuntimeException("Repository indexing failed", e);
        }
    }

    private String decodeGithubContent(Map<String, Object> fileData) {

        Object contentObject = fileData.get("content");

        if (contentObject == null) {
            return "";
        }

        String encoded = String.valueOf(contentObject)
                .replace("\n", "")
                .replace("\r", "");

        byte[] decoded = Base64.getDecoder().decode(encoded);

        return new String(decoded, StandardCharsets.UTF_8);
    }

    private String detectLanguage(String path) {

        int dotIndex = path.lastIndexOf(".");

        if (dotIndex == -1) {
            return "unknown";
        }

        return path.substring(dotIndex + 1).toLowerCase();
    }

    private String createMetadata(
            RepositoryEntity repository,
            String path,
            String language
    ) {
        return """
                {
                  "repositoryId": "%s",
                  "repositoryName": "%s",
                  "owner": "%s",
                  "filePath": "%s",
                  "language": "%s"
                }
                """.formatted(
                repository.getId(),
                repository.getName(),
                repository.getOwner(),
                path,
                language
        );
    }
}
