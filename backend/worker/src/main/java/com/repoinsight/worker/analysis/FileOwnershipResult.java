package com.repoinsight.worker.analysis;

import java.time.Instant;

public record FileOwnershipResult(
		String filePath,
		String contributorKey,
		String displayName,
		int commitCount,
		int additions,
		int deletions,
		Instant lastModifiedAt,
		double ownershipPercent) {
}