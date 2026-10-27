package com.example.controller;

import com.example.dto.RepoResponse;
import com.example.security.AppUserPrinciple;
import com.example.security.CurrentUser;
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
    private final CurrentUser currentUser;


    // Sync repos from GitHub for the logged-in user
    @PostMapping("/sync/{userId}")
    public ResponseEntity<List<RepoResponse>> syncRepositories(
            @PathVariable UUID userId
    ) {
        // Ensure the requesting user can only sync their own repos
        AppUserPrinciple principle = currentUser.require();
        if (!principle.getUser().getId().equals(userId)) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(
                repositoryService.syncRepositories(userId)
        );
    }


    // Start indexing — scoped to logged-in user's repos only
    @PostMapping("/{repositoryId}/index")
    public ResponseEntity<String> indexRepository(
            @PathVariable UUID repositoryId) {

        AppUserPrinciple principle = currentUser.require();
        UUID userId = principle.getUser().getId();

        // Verify ownership before indexing
        repositoryService.getRepository(repositoryId, userId);

        indexingService.indexRepository(repositoryId);

        return ResponseEntity.ok("Repository indexing started");
    }


    // Ask a question — scoped to logged-in user's repos only
    @PostMapping("/{repositoryId}/ask")
    public ResponseEntity<String> askRepository(
            @PathVariable UUID repositoryId,
            @RequestBody String question) {

        AppUserPrinciple principle = currentUser.require();
        UUID userId = principle.getUser().getId();

        // Verify ownership before allowing chat
        repositoryService.getRepository(repositoryId, userId);

        String answer = ragService.ask(repositoryId, question);

        return ResponseEntity.ok(answer);
    }


    // Get only the logged-in user's repositories
    @GetMapping
    public ResponseEntity<List<RepoResponse>> getRepositories() {

        AppUserPrinciple principle = currentUser.require();
        UUID userId = principle.getUser().getId();

        return ResponseEntity.ok(
                repositoryService.getRepositoriesForUser(userId)
        );
    }


    // Get a single repo — must belong to the logged-in user
    @GetMapping("/{id}")
    public ResponseEntity<RepoResponse> getRepository(
            @PathVariable UUID id
    ) {
        AppUserPrinciple principle = currentUser.require();
        UUID userId = principle.getUser().getId();

        return ResponseEntity.ok(
                repositoryService.getRepository(id, userId)
        );
    }
}
