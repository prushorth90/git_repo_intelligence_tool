package com.repoinsight.api.service;

import java.time.Instant;

public record GitHubRepository(
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
		Instant updatedAt) {
}