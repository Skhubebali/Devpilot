package com.example.service.indexing;

import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class CodeFileFilter {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            ".java",
            ".js",
            ".jsx",
            ".ts",
            ".tsx",
            ".py",
            ".cpp",
            ".c",
            ".h",
            ".hpp",
            ".cs",
            ".go",
            ".rs",
            ".php",
            ".rb",
            ".kt",
            ".kts",
            ".swift",
            ".html",
            ".css",
            ".scss",
            ".sql",
            ".xml",
            ".yml",
            ".yaml",
            ".json",
            ".md"
    );

    private static final Set<String> IGNORED_PATHS = Set.of(
            "node_modules/",
            ".git/",
            ".next/",
            "target/",
            "build/",
            "dist/",
            "out/",
            "coverage/"
    );

    private static final Set<String> IGNORED_FILES = Set.of(
            ".env",
            ".env.local",
            ".env.production",
            "package-lock.json",
            "yarn.lock",
            "pnpm-lock.yaml"
    );

    public boolean shouldIndex(String path) {

        if (path == null || path.isBlank()) {
            return false;
        }

        String normalizedPath = path.toLowerCase();

        // Ignore unwanted directories
        for (String ignoredPath : IGNORED_PATHS) {
            if (normalizedPath.contains(ignoredPath)) {
                return false;
            }
        }

        // Ignore specific files
        String fileName = normalizedPath.substring(
                normalizedPath.lastIndexOf("/") + 1
        );

        if (IGNORED_FILES.contains(fileName)) {
            return false;
        }

        // Check extension
        return ALLOWED_EXTENSIONS.stream()
                .anyMatch(normalizedPath::endsWith);
    }
}