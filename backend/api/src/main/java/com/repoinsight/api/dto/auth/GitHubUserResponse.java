package com.repoinsight.api.dto.auth;

import java.time.Instant;

import com.repoinsight.api.domain.GitHubConnection;

public record GitHubUserResponse(
		boolean configured,
		boolean connected,
		String login,
		String avatarUrl,
		Instant connectedAt) {

	public static GitHubUserResponse disconnected(boolean configured) {
		return new GitHubUserResponse(configured, false, null, null, null);
	}

	public static GitHubUserResponse connected(GitHubConnection connection) {
		return new GitHubUserResponse(
				true,
				true,
				connection.getGithubLogin(),
				connection.getAvatarUrl(),
				connection.getConnectedAt());
	}
}