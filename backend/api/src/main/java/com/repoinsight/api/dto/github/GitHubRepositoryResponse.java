package com.repoinsight.api.dto.github;

import java.time.Instant;

import com.repoinsight.api.service.GitHubRepository;

public record GitHubRepositoryResponse(
		long id,
		String owner,
		String name,
		String fullName,
		String htmlUrl,
		String defaultBranch,
		String visibility,
		boolean privateRepository,
		String primaryLanguage,
		int stars,
		int forks,
		Instant updatedAt,
		boolean imported) {

	public static GitHubRepositoryResponse from(GitHubRepository repository, boolean imported) {
		return new GitHubRepositoryResponse(
				repository.id(), repository.owner(), repository.name(), repository.fullName(), repository.htmlUrl(),
				repository.defaultBranch(), repository.visibility(), repository.privateRepository(),
				repository.primaryLanguage(), repository.stars(), repository.forks(), repository.updatedAt(), imported);
	}
}