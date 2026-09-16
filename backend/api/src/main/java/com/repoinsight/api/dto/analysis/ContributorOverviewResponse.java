package com.repoinsight.api.dto.analysis;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ContributorOverviewResponse(
		UUID analysisId,
		Instant analyzedAt,
		int repositoryBusFactor,
		int concentratedFileCount,
		List<ContributorOwnershipResponse> contributors,
		List<ConcentratedFileResponse> concentratedFiles) {

	public static ContributorOverviewResponse empty() {
		return new ContributorOverviewResponse(null, null, 0, 0, List.of(), List.of());
	}
}