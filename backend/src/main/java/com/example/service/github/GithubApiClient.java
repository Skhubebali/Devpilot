package com.example.service.github;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GithubApiClient {

    private static final String API_BASE = "https://api.github.com";

    private static final ParameterizedTypeReference<List<Map<String, Object>>> LIST_MAP =
            new ParameterizedTypeReference<>() {};

    private static final ParameterizedTypeReference<Map<String, Object>> MAP =
            new ParameterizedTypeReference<>() {};


    private final RestClient.Builder restClient;


    private RestClient client(String accessToken) {

        return restClient
                .baseUrl(API_BASE)
                .defaultHeader(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + accessToken
                )
                .defaultHeader(
                        HttpHeaders.ACCEPT,
                        "application/vnd.github+json"
                )
                .defaultHeader(
                        "X-GitHub-Api-Version",
                        "2022-11-28"
                )
                .build();
    }


    // Get authenticated GitHub user
    public Map<String, Object> getCurrentUser(String accessToken) {

        return client(accessToken)
                .get()
                .uri("/user")
                .retrieve()
                .body(MAP);
    }


    // Get user's repositories
    public List<Map<String, Object>> getRepositories(String accessToken) {

        return client(accessToken)
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/user/repos")
                        .queryParam("per_page", 100)
                        .queryParam("sort", "updated")
                        .build())
                .retrieve()
                .body(LIST_MAP);
    }


    // Get a specific repository
    public Map<String, Object> getRepository(
            String accessToken,
            String owner,
            String repo
    ) {

        return client(accessToken)
                .get()
                .uri("/repos/{owner}/{repo}", owner, repo)
                .retrieve()
                .body(MAP);
    }


    // Get repository contents (single directory listing)
    public List<Map<String, Object>> getContents(
            String accessToken,
            String owner,
            String repo,
            String path
    ) {

        String uri = (path == null || path.isBlank())
                ? "/repos/{owner}/{repo}/contents"
                : "/repos/{owner}/{repo}/contents/{path}";

        if (path == null || path.isBlank()) {
            return client(accessToken)
                    .get()
                    .uri(uri, owner, repo)
                    .retrieve()
                    .body(LIST_MAP);
        }

        return client(accessToken)
                .get()
                .uri(uri, owner, repo, path)
                .retrieve()
                .body(LIST_MAP);
    }


    /**
     * Get ALL files in the repository in a single API call using the Git Trees API
     * (recursive=1). This is vastly faster than the old recursive getContents()
     * approach which made one HTTP request per directory.
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getTreeFiles(
            String accessToken,
            String owner,
            String repo,
            String branch
    ) {
        try {
            Map<String, Object> treeResponse = client(accessToken)
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/repos/{owner}/{repo}/git/trees/{branch}")
                            .queryParam("recursive", "1")
                            .build(owner, repo, branch))
                    .retrieve()
                    .body(MAP);

            if (treeResponse == null) {
                return List.of();
            }

            List<Map<String, Object>> tree =
                    (List<Map<String, Object>>) treeResponse.get("tree");

            if (tree == null) {
                return List.of();
            }

            // Return only blob (file) entries — filter out trees (directories)
            return tree.stream()
                    .filter(item -> "blob".equals(item.get("type")))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.warn("Git Trees API failed for {}/{}, falling back to recursive listing: {}", owner, repo, e.getMessage());
            // Fallback to the recursive contents approach if tree API fails
            return getContentsRecursive(accessToken, owner, repo, "");
        }
    }


    /**
     * Recursive fallback — walks the repository tree directory by directory.
     * Only used if the Git Trees API call fails.
     */
    private List<Map<String, Object>> getContentsRecursive(
            String accessToken,
            String owner,
            String repo,
            String path
    ) {
        List<Map<String, Object>> result = new ArrayList<>();

        List<Map<String, Object>> contents = getContents(accessToken, owner, repo, path);
        if (contents == null) return result;

        for (Map<String, Object> item : contents) {
            String type = String.valueOf(item.get("type"));
            String itemPath = String.valueOf(item.get("path"));

            if ("file".equals(type)) {
                result.add(item);
            } else if ("dir".equals(type)) {
                result.addAll(getContentsRecursive(accessToken, owner, repo, itemPath));
            }
        }

        return result;
    }


    // Get branches
    public List<Map<String, Object>> getBranches(
            String accessToken,
            String owner,
            String repo
    ) {

        return client(accessToken)
                .get()
                .uri("/repos/{owner}/{repo}/branches", owner, repo)
                .retrieve()
                .body(LIST_MAP);
    }


    // Get commits
    public List<Map<String, Object>> getCommits(
            String accessToken,
            String owner,
            String repo
    ) {

        return client(accessToken)
                .get()
                .uri("/repos/{owner}/{repo}/commits", owner, repo)
                .retrieve()
                .body(LIST_MAP);
    }


    // Get a file's content
    public Map<String, Object> getFile(
            String accessToken,
            String owner,
            String repo,
            String path
    ) {

        return client(accessToken)
                .get()
                .uri("/repos/{owner}/{repo}/contents/{path}",
                        owner,
                        repo,
                        path)
                .retrieve()
                .body(MAP);
    }
}
