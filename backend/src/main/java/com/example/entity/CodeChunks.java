package com.example.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "code_chunks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CodeChunks {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "repository_id", nullable = false)
    private UUID repositoryId;

    @Column(name = "file_path", nullable = false, length = 1000)
    private String filePath;

    @Column(name = "chunk_index", nullable = false)
    private int chunkIndex;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "language", length = 50)
    private String language;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    /**
     * pgvector column.
     *
     * The column DDL is vector(1536). Without a type hint Hibernate 6 binds
     * a String field as VARCHAR, which pgvector rejects. Annotating with
     * @JdbcTypeCode(SqlTypes.OTHER) makes Hibernate call setObject() instead
     * of setString(), so the pgvector JDBC driver receives the bracket-notation
     * string "[x,y,z]" and inserts it as the native vector type — no cast needed.
     */
    @Column(name = "embedding", columnDefinition = "vector(1536)")
    @JdbcTypeCode(SqlTypes.OTHER)
    private String embedding;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
