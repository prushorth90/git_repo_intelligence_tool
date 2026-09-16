package com.repoinsight.api.service.exception;

import java.time.Instant;

public class GitHubRateLimitException extends RuntimeException {

	private final Instant resetAt;

	public GitHubRateLimitException(Instant resetAt) {
		super("GitHub API rate limit exceeded. Try again after " + resetAt + ".");
		this.resetAt = resetAt;
	}

	public Instant getResetAt() {
		return resetAt;
	}
}