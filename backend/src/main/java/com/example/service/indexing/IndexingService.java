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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.*;

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

    @Transactional
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
            String accessToken =
                    userService.decryptAccessToken(user);

            // 4. Delete old chunks
            codeChunkRepo.deleteByRepositoryId(repositoryId);

            // 5. Get all repository files recursively
            List<Map<String, Object>> files =
                    getAllFiles(
                            accessToken,
                            repository.getOwner(),
                            repository.getName(),
                            ""
                    );

            repository.setTotalFiles(files.size());
            repositoryRepo.save(repository);

            int processedFiles = 0;
            int totalChunks = 0;

            // 6. Process every file
            for (Map<String, Object> file : files) {

                String path = String.valueOf(file.get("path"));

                // 7. Filter files
                if (!codeFileFilter.shouldIndex(path)) {
                    continue;
                }

                try {

                    // 8. Get actual file content
                    Map<String, Object> fileData =
                            githubApiClient.getFile(
                                    accessToken,
                                    repository.getOwner(),
                                    repository.getName(),
                                    path
                            );

                    String content =
                            decodeGithubContent(fileData);

                    if (content.isBlank()) {
                        continue;
                    }

                    // 9. Detect language
                    String language =
                            detectLanguage(path);

                    // 10. Create chunks
                    List<String> chunks =
                            codeChunker.chunk(content);

                    // 11. Save each chunk
                    for (int i = 0; i < chunks.size(); i++) {

                        String chunkText = chunks.get(i);

                        // 12. Generate embedding
                        float[] embedding =
                                embeddingService.generateEmbedding(
                                        chunkText
                                );

                        CodeChunks codeChunk =
                                CodeChunks.builder()
                                        .repositoryId(repositoryId)
                                        .filePath(path)
                                        .chunkIndex(i)
                                        .content(chunkText)
                                        .language(language)
                                        .metadata(createMetadata(
                                                repository,
                                                path,
                                                language
                                        ))
                                        .embedding(
                                                Arrays.toString(embedding)
                                        )
                                        .build();

                        codeChunkRepo.save(codeChunk);

                        totalChunks++;
                    }

                    processedFiles++;

                    repository.setFileProcessed(processedFiles);
                    repository.setChunkCount(totalChunks);

                    repositoryRepo.save(repository);

                } catch (Exception fileError) {

                    System.err.println(
                            "Failed to index file: "
                                    + path
                                    + " -> "
                                    + fileError.getMessage()
                    );
                }
            }

            // 13. Mark repository ready
            repository.setIndexStatus(IndexStatus.READY);
            repository.setFileProcessed(processedFiles);
            repository.setChunkCount(totalChunks);

            repositoryRepo.save(repository);

        } catch (Exception e) {

            repository.setIndexStatus(IndexStatus.FAILED);
            repository.setErrorMsg(e.getMessage());

            repositoryRepo.save(repository);

            throw new RuntimeException(
                    "Repository indexing failed",
                    e
            );
        }
    }

    private List<Map<String, Object>> getAllFiles(
            String accessToken,
            String owner,
            String repo,
            String path
    ) {

        List<Map<String, Object>> result =
                new ArrayList<>();

        List<Map<String, Object>> contents =
                githubApiClient.getContents(
                        accessToken,
                        owner,
                        repo,
                        path
                );

        for (Map<String, Object> item : contents) {

            String type =
                    String.valueOf(item.get("type"));

            String itemPath =
                    String.valueOf(item.get("path"));

            if ("file".equals(type)) {

                result.add(item);

            } else if ("dir".equals(type)) {

                result.addAll(
                        getAllFiles(
                                accessToken,
                                owner,
                                repo,
                                itemPath
                        )
                );
            }
        }

        return result;
    }

    private String decodeGithubContent(
            Map<String, Object> fileData
    ) {

        Object contentObject =
                fileData.get("content");

        if (contentObject == null) {
            return "";
        }

        String encoded =
                String.valueOf(contentObject)
                        .replace("\n", "")
                        .replace("\r", "");

        byte[] decoded =
                Base64.getDecoder().decode(encoded);

        return new String(
                decoded,
                StandardCharsets.UTF_8
        );
    }

    private String detectLanguage(String path) {

        int dotIndex =
                path.lastIndexOf(".");

        if (dotIndex == -1) {
            return "unknown";
        }

        return path
                .substring(dotIndex + 1)
                .toLowerCase();
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