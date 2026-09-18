package com.example.service.github;

import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.*;

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


    // Get repository contents
    public List<Map<String, Object>> getContents(
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
                .body(LIST_MAP);
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