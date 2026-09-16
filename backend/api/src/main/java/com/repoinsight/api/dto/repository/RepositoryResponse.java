package com.repoinsight.api.dto.repository;

import java.time.Instant;
import java.util.UUID;

import com.repoinsight.api.domain.Repository;

public record RepositoryResponse(
		UUID id,
		String owner,
		String name,
		String fullName,
		String githubUrl,
		Long githubRepositoryId,
		String defaultBranch,
		boolean privateRepository,
		String visibility,
		String primaryLanguage,
		int stars,
		int forks,
		Instant githubUpdatedAt,
		Instant connectedAt) {

	public static RepositoryResponse from(Repository repository) {
		return new RepositoryResponse(
				repository.getId(),
				repository.getOwner(),
				repository.getName(),
				repository.getFullName(),
				repository.getGithubUrl(),
				repository.getGithubRepositoryId(),
				repository.getDefaultBranch(),
				repository.isPrivateRepository(),
				repository.getVisibility(),
				repository.getPrimaryLanguage(),
				repository.getStars(),
				repository.getForks(),
				repository.getGithubUpdatedAt(),
				repository.getConnectedAt());
	}
}