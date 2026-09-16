package com.repoinsight.api.dto.analysis;

import java.time.Instant;
import java.util.List;

public record ContributorOwnershipResponse(
		String contributorKey,
		String displayName,
		int totalCommits,
		int filesTouched,
		int additions,
		int deletions,
		double estimatedOwnershipPercent,
		Instant lastActivityAt,
		List<String> primaryModules) {
}