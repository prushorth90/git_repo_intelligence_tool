package com.repoinsight.api.dto.analysis;

public record ConcentratedFileResponse(
		String filePath,
		String language,
		String topContributorName,
		double topOwnershipPercent,
		int busFactor,
		long totalChurn) {
}