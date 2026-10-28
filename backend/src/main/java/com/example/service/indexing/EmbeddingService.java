package com.example.service.indexing;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmbeddingService {

    private final EmbeddingModel embeddingModel;

    /**
     * Generate embedding for a single text.
     */
    public float[] generateEmbedding(String text) {

        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Text cannot be empty");
        }

        return embeddingModel.embed(text);
    }

    /**
     * Generate embeddings for a list of texts in a single batch call.
     * Reduces round-trips to the embedding API from one-per-chunk to one-per-file.
     *
     * Spring AI: EmbeddingModel.embedForResponse(List<String>) returns EmbeddingResponse
     * whose results stream each yields Embedding whose getOutput() is already float[].
     */
    public List<float[]> generateEmbeddings(List<String> texts) {

        if (texts == null || texts.isEmpty()) {
            return List.of();
        }

        return embeddingModel
                .embedForResponse(texts)
                .getResults()
                .stream()
                .map(embedding -> embedding.getOutput())
                .toList();
    }
}
