package com.example.repository;

import com.example.entity.RepositoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RepositoryRepo extends JpaRepository<RepositoryEntity, UUID> {

    RepositoryEntity findByGithubRepoId(Long githubRepoId);

    List<RepositoryEntity> findByOwner(String owner);

    boolean existsByGithubRepoId(Long githubRepoId);

    // Only fetch repos belonging to a specific user
    List<RepositoryEntity> findByUserId(UUID userId);

    // Fetch a single repo only if it belongs to the given user
    Optional<RepositoryEntity> findByIdAndUserId(UUID id, UUID userId);
}
