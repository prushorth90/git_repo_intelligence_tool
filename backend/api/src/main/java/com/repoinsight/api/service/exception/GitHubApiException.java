package com.repoinsight.api.service.exception;

public class GitHubApiException extends RuntimeException {

	public GitHubApiException(String message) {
		super(message);
	}
}