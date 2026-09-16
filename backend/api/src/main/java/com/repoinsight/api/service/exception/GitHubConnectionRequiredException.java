package com.repoinsight.api.service.exception;

public class GitHubConnectionRequiredException extends RuntimeException {

	public GitHubConnectionRequiredException() {
		super("A connected GitHub account is required.");
	}
}