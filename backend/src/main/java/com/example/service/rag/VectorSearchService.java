package com.example.service.rag;


import com.example.entity.CodeChunks;
import com.example.repository.CodeChunkRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VectorSearchService {

    private final EmbeddingModel embeddingModel;
    private final CodeChunkRepo codeChunkRepo;

    public List<CodeChunks> search(
            UUID repositoryId,
            String query,
            int topK
    ) {

        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException(
                    "Query cannot be empty"
            );
        }

        if (topK <= 0) {
            topK = 5;
        }

        // 1. Convert user question into embedding
        float[] queryEmbedding =
                embeddingModel.embed(query);

        // 2. Convert float[] → pgvector format
        String vector =
                toPgVector(queryEmbedding);

        // 3. Similarity search
        return codeChunkRepo.similaritySearch(
                repositoryId,
                vector,
                topK
        );
    }

    private String toPgVector(float[] embedding) {

        StringBuilder vector =
                new StringBuilder("[");

        for (int i = 0; i < embedding.length; i++) {

            if (i > 0) {
                vector.append(",");
            }

            vector.append(embedding[i]);
        }

        vector.append("]");

        return vector.toString();
    }
}