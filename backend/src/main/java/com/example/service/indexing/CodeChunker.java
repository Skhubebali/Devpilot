package com.example.service.indexing;

import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class CodeChunker {

    private static final int CHUNK_SIZE = 1500;
    private static final int OVERLAP = 200;

    public List<String> chunk(String content) {

        List<String> chunks = new ArrayList<>();

        if (content == null || content.isBlank()) {
            return chunks;
        }

        int start = 0;

        while (start < content.length()) {

            int end = Math.min(
                    start + CHUNK_SIZE,
                    content.length()
            );

            // Try to end the chunk at a newline
            if (end < content.length()) {
                int newline = content.lastIndexOf("\n", end);

                if (newline > start) {
                    end = newline;
                }
            }

            String chunk = content.substring(start, end).trim();

            if (!chunk.isBlank()) {
                chunks.add(chunk);
            }

            if (end >= content.length()) {
                break;
            }

            start = Math.max(end - OVERLAP, start + 1);
        }

        return chunks;
    }
    private Map<String, Object> baseMetadata(
            UUID repositoryId,
            String path,
            String language
    ) {
        Map<String, Object> metadata = new HashMap<>();

        metadata.put("repositoryId", repositoryId.toString());
        metadata.put("path", path);
        metadata.put("language", language);

        return metadata;
    }
}