package com.repoinsight.api.service;

import java.time.Instant;
import java.util.List;

public record GitHubRepositoryPage(
		List<GitHubRepository> repositories,
		int page,
		int perPage,
		boolean hasNextPage,
		int rateLimitRemaining,
		Instant rateLimitResetAt) {
}