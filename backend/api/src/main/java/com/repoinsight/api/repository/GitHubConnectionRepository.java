package com.repoinsight.api.repository;

import java.util.Optional;
import java.util.UUID;

import com.repoinsight.api.domain.GitHubConnection;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GitHubConnectionRepository extends JpaRepository<GitHubConnection, UUID> {
	Optional<GitHubConnection> findByGithubUserId(Long githubUserId);
}