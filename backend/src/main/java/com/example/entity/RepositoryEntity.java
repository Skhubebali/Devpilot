package com.example.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "repositories",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id","github_repo_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RepositoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "github_repo_id", nullable = false, unique = true)
    private Long githubRepoId;

    @Column(name = "owner", nullable = false, length = 100)
    private String owner;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "fullname", nullable = false, length = 500)
    private String fullname;

    @Column(name = "is_private", nullable = false)
    private boolean isPrivate;

    @Column(name = "default_branch", nullable = false, length = 100)
    private String defaultBranch;

    @Column(name = "language", length = 100)
    private String language;

    @Column(name = "html_url", nullable = false, length = 500)
    private String htmlUrl;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "index_status", nullable = false, length = 30)
    @Builder.Default
    private IndexStatus indexStatus = IndexStatus.PENDING;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "chunk_count", nullable = false)
    @Builder.Default
    private int chunkCount = 0;

    @Column(name = "file_processed", nullable = false)
    @Builder.Default
    private int fileProcessed = 0;

    @Column(name = "total_files", nullable = false)
    @Builder.Default
    private int totalFiles = 0;

    @Column(name = "error_msg", columnDefinition = "TEXT")
    private String errorMsg;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();

        if (createdAt == null) {
            createdAt = now;
        }

        updatedAt = now;

        if (indexStatus == null) {
            indexStatus = IndexStatus.PENDING;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}