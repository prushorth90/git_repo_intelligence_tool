package com.repoinsight.api.service.exception;

public class AnalysisJobAlreadyActiveException extends RuntimeException {

	public AnalysisJobAlreadyActiveException() {
		super("This repository already has a queued or running analysis job.");
	}
}