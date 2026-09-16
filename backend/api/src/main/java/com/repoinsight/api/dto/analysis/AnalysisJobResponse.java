package com.repoinsight.api.dto.analysis;

import java.time.Instant;
import java.util.UUID;

import com.repoinsight.api.domain.AnalysisJob;
import com.repoinsight.api.domain.AnalysisJobStatus;

public record AnalysisJobResponse(
		UUID id,
		UUID repositoryId,
		AnalysisJobStatus status,
		Instant requestedAt,
		Instant startedAt,
		Instant completedAt,
		String failureMessage) {

	public static AnalysisJobResponse from(AnalysisJob job) {
		return new AnalysisJobResponse(
				job.getId(),
				job.getRepository().getId(),
				job.getStatus(),
				job.getRequestedAt(),
				job.getStartedAt(),
				job.getCompletedAt(),
				job.getFailureMessage());
	}
}