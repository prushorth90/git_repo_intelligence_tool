package com.repoinsight.api.service.exception;

public class GitHubOAuthNotConfiguredException extends RuntimeException {

	public GitHubOAuthNotConfiguredException() {
		super("GitHub OAuth is not configured on the server.");
	}
}