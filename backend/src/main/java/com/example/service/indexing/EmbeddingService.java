package com.example.service.indexing;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
     * Much faster than calling generateEmbedding() in a loop because it
     * reduces the number of round-trips to the embedding API.
     */
    public List<float[]> generateEmbeddings(List<String> texts) {

        if (texts == null || texts.isEmpty()) {
            return List.of();
        }

        List<float[]> results = new ArrayList<>(texts.size());

        // Spring AI EmbeddingModel.embedForResponse supports batch embedding
        List<List<Double>> batchResult = embeddingModel
                .embedForResponse(texts)
                .getResults()
                .stream()
                .map(output -> output.getOutput())
                .toList();

        for (List<Double> doubles : batchResult) {
            float[] arr = new float[doubles.size()];
            for (int i = 0; i < doubles.size(); i++) {
                arr[i] = doubles.get(i).floatValue();
            }
            results.add(arr);
        }

        return results;
    }
}
