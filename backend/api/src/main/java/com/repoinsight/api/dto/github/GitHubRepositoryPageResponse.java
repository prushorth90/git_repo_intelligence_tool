package com.repoinsight.api.dto.github;

import java.time.Instant;
import java.util.List;

import com.repoinsight.api.service.GitHubRepositoryCatalogPage;

public record GitHubRepositoryPageResponse(
		List<GitHubRepositoryResponse> repositories,
		int page,
		int perPage,
		boolean hasNextPage,
		int rateLimitRemaining,
		Instant rateLimitResetAt) {

	public static GitHubRepositoryPageResponse from(GitHubRepositoryCatalogPage catalog) {
		return new GitHubRepositoryPageResponse(
				catalog.page().repositories().stream()
						.map(repository -> GitHubRepositoryResponse.from(
								repository, catalog.importedRepositoryIds().contains(repository.id())))
						.toList(),
				catalog.page().page(),
				catalog.page().perPage(),
				catalog.page().hasNextPage(),
				catalog.page().rateLimitRemaining(),
				catalog.page().rateLimitResetAt());
	}
}