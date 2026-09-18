package com.example.dto;

import com.example.entity.IndexStatus;

import java.time.Instant;
import java.util.UUID;

public record RepoResponse(
        UUID id,
        Long githubRepoId,
        String owner,
        String name,
        String fullname,
        boolean isPrivate,
        String defaultBranch,
        String language,
        String htmlUrl,
        String description,
        IndexStatus indexStatus,
        Instant createdAt,
        int chunkCount,
        int fileProcessed,
        int totalFiles,
        String errorMsg,
        Instant updatedAt
) {
}