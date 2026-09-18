package com.example.service.rag;


import com.example.entity.CodeChunks;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RagService {

    private final VectorSearchService vectorSearchService;
    private final ChatClient.Builder chatClientBuilder;

    public String ask(
            UUID repositoryId,
            String question
    ) {

        // 1. Find relevant code chunks
        List<CodeChunks> chunks =
                vectorSearchService.search(
                        repositoryId,
                        question,
                        5
                );

        if (chunks.isEmpty()) {
            return "I could not find relevant code in this repository.";
        }

        // 2. Build context
        String context = buildContext(chunks);

        // 3. Create prompt
        String prompt = """
                You are DevPilot, an AI coding assistant.

                Answer the user's question using ONLY the repository
                context provided below.

                If the answer is not present in the context,
                clearly say that you could not find it.

                Repository Context:
                -------------------
                %s
                -------------------

                User Question:
                %s

                Give a clear and concise answer.
                Mention the relevant file paths when possible.
                """.formatted(context, question);

        // 4. Ask LLM
        ChatClient chatClient =
                chatClientBuilder.build();

        return chatClient
                .prompt()
                .user(prompt)
                .call()
                .content();
    }

    private String buildContext(
            List<CodeChunks> chunks
    ) {

        StringBuilder context =
                new StringBuilder();

        for (CodeChunks chunk : chunks) {

            context.append("""
                    
                    File: %s
                    Language: %s
                    Chunk: %d
                    
                    %s
                    
                    """.formatted(
                    chunk.getFilePath(),
                    chunk.getLanguage(),
                    chunk.getChunkIndex(),
                    chunk.getContent()
            ));
        }

        return context.toString();
    }
}
