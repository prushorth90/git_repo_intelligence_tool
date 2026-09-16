package com.repoinsight.api.service.exception;

public class AnalysisJobNotCancellableException extends RuntimeException {

	public AnalysisJobNotCancellableException() {
		super("Only queued or running analysis jobs can be cancelled.");
	}
}