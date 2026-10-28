package com.example.repository;

import com.example.entity.CodeChunks;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CodeChunkRepo extends JpaRepository<CodeChunks, UUID> {

    List<CodeChunks> findByRepositoryId(UUID repositoryId);

    void deleteByRepositoryId(UUID repositoryId);

    long countByRepositoryId(UUID repositoryId);

    @Query(value = """
        SELECT id, repository_id, file_path, chunk_index, content, language, metadata, cast(embedding as text) as embedding, created_at
        FROM code_chunks
        WHERE repository_id = :repositoryId
        ORDER BY embedding <=> CAST(:embedding AS vector)
        LIMIT :topK
        """, nativeQuery = true)
    List<CodeChunks> similaritySearch(
            @Param("repositoryId") UUID repositoryId,
            @Param("embedding") String embedding,
            @Param("topK") int topK
    );
}
