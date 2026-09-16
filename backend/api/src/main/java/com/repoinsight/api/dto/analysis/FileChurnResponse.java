package com.repoinsight.api.dto.analysis;

import java.time.Instant;

import com.repoinsight.api.domain.FileMetric;

public record FileChurnResponse(
		String filePath,
		String language,
		int commitCount,
		int additions,
		int deletions,
		int uniqueContributors,
		Instant lastModifiedAt,
		long totalChurn) {

	public static FileChurnResponse from(FileMetric metric) {
		return new FileChurnResponse(
				metric.getFilePath(), metric.getLanguage(), metric.getCommitCount(), metric.getAdditions(),
				metric.getDeletions(), metric.getContributorCount(), metric.getLastModifiedAt(), metric.getTotalChurn());
	}
}