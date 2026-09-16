package com.repoinsight.api.service.exception;

import java.util.UUID;

public class AnalysisJobNotFoundException extends RuntimeException {

	public AnalysisJobNotFoundException(UUID jobId) {
		super("Analysis job was not found: " + jobId);
	}
}