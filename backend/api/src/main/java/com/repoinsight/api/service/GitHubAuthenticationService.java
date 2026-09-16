package com.repoinsight.api.service;

import java.util.Optional;
import java.util.Set;

import com.repoinsight.api.domain.GitHubConnection;

import org.springframework.security.oauth2.core.user.OAuth2User;

public interface GitHubAuthenticationService {

	GitHubConnection connect(OAuth2User principal, String accessToken, Set<String> scopes);

	Optional<GitHubConnection> findByGithubUserId(Long githubUserId);

	void disconnect(Long githubUserId);

}