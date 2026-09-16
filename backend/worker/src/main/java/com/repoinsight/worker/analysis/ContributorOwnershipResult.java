package com.repoinsight.worker.analysis;

import java.time.Instant;
import java.util.List;

public record ContributorOwnershipResult(
		String contributorKey,
		String displayName,
		int totalCommits,
		int filesTouched,
		int additions,
		int deletions,
		double estimatedOwnershipPercent,
		double weightedScore,
		Instant lastActivityAt,
		List<String> primaryModules) {
}