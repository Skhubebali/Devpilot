package com.example.controller;

import com.example.dto.RepoResponse;
import com.example.service.github.RepositoryService;
import com.example.service.indexing.IndexingService;
import com.example.service.rag.RagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/repositories")
@RequiredArgsConstructor
public class RepoController {

    private final RepositoryService repositoryService;
    private final IndexingService indexingService;
    private final RagService ragService;


    // GitHub se repositories sync karke DB mein save karega
    @PostMapping("/sync/{userId}")
    public ResponseEntity<List<RepoResponse>> syncRepositories(
            @PathVariable UUID userId
    ) {
        return ResponseEntity.ok(
                repositoryService.syncRepositories(userId)
        );
    }


    @PostMapping("/{repositoryId}/index")
    public ResponseEntity<String> indexRepository(
            @PathVariable UUID repositoryId) {

        indexingService.indexRepository(repositoryId);

        return ResponseEntity.ok(
                "Repository indexing started"
        );
    }


    @PostMapping("/{repositoryId}/ask")
    public ResponseEntity<String> askRepository(
            @PathVariable UUID repositoryId,
            @RequestBody String question) {

        String answer = ragService.ask(
                repositoryId,
                question
        );

        return ResponseEntity.ok(answer);
    }

    // DB se saari repositories
    @GetMapping
    public ResponseEntity<List<RepoResponse>> getRepositories() {

        return ResponseEntity.ok(
                repositoryService.getAllRepositories()
        );
    }


    // Ek repository ki details
    @GetMapping("/{id}")
    public ResponseEntity<RepoResponse> getRepository(
            @PathVariable UUID id
    ) {

        return ResponseEntity.ok(
                repositoryService.getRepository(id)
        );
    }


    @GetMapping("/owner/{owner}")
    public ResponseEntity<List<RepoResponse>> getByOwner(
            @PathVariable String owner
    ) {
        return ResponseEntity.ok(
                repositoryService.getRepositoriesByOwner(owner)
        );
    }



}