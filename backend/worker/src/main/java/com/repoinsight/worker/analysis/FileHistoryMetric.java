package com.repoinsight.worker.analysis;

import java.time.Instant;

public record FileHistoryMetric(
		String filePath,
		String language,
		int commitCount,
		int additions,
		int deletions,
		int uniqueContributors,
		Instant lastModifiedAt) {

	public long totalChurn() {
		return (long) additions + deletions;
	}
}