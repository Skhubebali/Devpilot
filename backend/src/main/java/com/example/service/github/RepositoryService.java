package com.example.service.github;

import com.example.dto.RepoResponse;
import com.example.entity.RepositoryEntity;
import com.example.entity.User;
import com.example.exception.UnAuthException;
import com.example.repository.RepositoryRepo;
import com.example.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RepositoryService {

    private final RepositoryRepo repositoryRepo;
    private final UserService userService;
    private final GithubApiClient githubApiClient;


    // =========================================================
    // SYNC GITHUB REPOSITORIES
    // =========================================================

    @Transactional
    public List<RepoResponse> syncRepositories(UUID userId) {

        // 1. Get user from database
        User user = userService.getById(userId);

        // 2. Get decrypted GitHub access token
        String accessToken = userService.decryptAccessToken(user);

        // 3. Get repositories from GitHub
        List<Map<String, Object>> githubRepositories =
                githubApiClient.getRepositories(accessToken);

        // 4. Save/update repositories
        return githubRepositories.stream()
                .map(repo -> saveRepository(repo, userId))
                .map(this::toResponse)
                .toList();
    }


    // =========================================================
    // SAVE / UPDATE SINGLE REPOSITORY
    // =========================================================

    @Transactional
    public RepositoryEntity saveRepository(
            Map<String, Object> repo,
            UUID userId
    ) {

        Long githubRepoId =
                ((Number) repo.get("id")).longValue();

        Map<String, Object> owner =
                (Map<String, Object>) repo.get("owner");

        String ownerName =
                owner != null && owner.get("login") != null
                        ? String.valueOf(owner.get("login"))
                        : "";

        RepositoryEntity repository =
                repositoryRepo.findByGithubRepoId(githubRepoId);

        if (repository == null) {
            repository = new RepositoryEntity();
        }

        repository.setUserId(userId);
        repository.setGithubRepoId(githubRepoId);
        repository.setOwner(ownerName);
        repository.setName(String.valueOf(repo.get("name")));
        repository.setFullname(String.valueOf(repo.get("full_name")));
        repository.setPrivate(Boolean.TRUE.equals(repo.get("private")));
        repository.setDefaultBranch(
                repo.get("default_branch") != null
                        ? String.valueOf(repo.get("default_branch"))
                        : "main"
        );
        repository.setLanguage(
                repo.get("language") != null
                        ? String.valueOf(repo.get("language"))
                        : null
        );
        repository.setHtmlUrl(
                repo.get("html_url") != null
                        ? String.valueOf(repo.get("html_url"))
                        : ""
        );
        repository.setDescription(
                repo.get("description") != null
                        ? String.valueOf(repo.get("description"))
                        : null
        );

        return repositoryRepo.save(repository);
    }


    // =========================================================
    // GET ALL REPOSITORIES — scoped to current user only
    // =========================================================

    @Transactional(readOnly = true)
    public List<RepoResponse> getRepositoriesForUser(UUID userId) {
        return repositoryRepo.findByUserId(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }


    // =========================================================
    // GET SINGLE REPOSITORY — must belong to current user
    // =========================================================

    @Transactional(readOnly = true)
    public RepoResponse getRepository(UUID id, UUID userId) {

        RepositoryEntity repository =
                repositoryRepo.findByIdAndUserId(id, userId)
                        .orElseThrow(() ->
                                new UnAuthException(
                                        "Repository not found or access denied"
                                )
                        );

        return toResponse(repository);
    }


    // =========================================================
    // GET REPOSITORY BY GITHUB REPOSITORY ID
    // =========================================================

    @Transactional(readOnly = true)
    public RepoResponse getByGithubRepoId(Long githubRepoId) {

        RepositoryEntity repository =
                repositoryRepo.findByGithubRepoId(githubRepoId);

        if (repository == null) {
            throw new IllegalArgumentException(
                    "GitHub repository not found: " + githubRepoId
            );
        }

        return toResponse(repository);
    }


    // =========================================================
    // GET REPOSITORIES BY OWNER
    // =========================================================

    @Transactional(readOnly = true)
    public List<RepoResponse> getRepositoriesByOwner(String owner) {
        return repositoryRepo.findByOwner(owner)
                .stream()
                .map(this::toResponse)
                .toList();
    }


    // =========================================================
    // ENTITY -> RESPONSE DTO
    // =========================================================

    private RepoResponse toResponse(RepositoryEntity repository) {
        return new RepoResponse(
                repository.getId(),
                repository.getGithubRepoId(),
                repository.getOwner(),
                repository.getName(),
                repository.getFullname(),
                repository.isPrivate(),
                repository.getDefaultBranch(),
                repository.getLanguage(),
                repository.getHtmlUrl(),
                repository.getDescription(),
                repository.getIndexStatus(),
                repository.getCreatedAt(),
                repository.getChunkCount(),
                repository.getFileProcessed(),
                repository.getTotalFiles(),
                repository.getErrorMsg(),
                repository.getUpdatedAt()
        );
    }
}
