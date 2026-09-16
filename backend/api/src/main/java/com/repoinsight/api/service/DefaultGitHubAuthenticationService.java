package com.repoinsight.api.service;

import java.util.Optional;
import java.util.Set;

import com.repoinsight.api.domain.GitHubConnection;
import com.repoinsight.api.domain.User;
import com.repoinsight.api.infrastructure.security.AccessTokenCipher;
import com.repoinsight.api.repository.GitHubConnectionRepository;
import com.repoinsight.api.repository.UserRepository;

import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DefaultGitHubAuthenticationService implements GitHubAuthenticationService {

	private final UserRepository userRepository;
	private final GitHubConnectionRepository connectionRepository;
	private final AccessTokenCipher accessTokenCipher;

	public DefaultGitHubAuthenticationService(
			UserRepository userRepository,
			GitHubConnectionRepository connectionRepository,
			AccessTokenCipher accessTokenCipher) {
		this.userRepository = userRepository;
		this.connectionRepository = connectionRepository;
		this.accessTokenCipher = accessTokenCipher;
	}

	@Override
	@Transactional
	public GitHubConnection connect(OAuth2User principal, String accessToken, Set<String> scopes) {
		Long githubUserId = requiredLong(principal, "id");
		String login = requiredString(principal, "login");
		String avatarUrl = optionalString(principal, "avatar_url", "");
		String email = optionalString(principal, "email", "github-" + githubUserId + "@users.noreply.github.com");
		String displayName = optionalString(principal, "name", login);
		String encryptedToken = accessTokenCipher.encrypt(accessToken);

		Optional<GitHubConnection> existingConnection = connectionRepository.findByGithubUserId(githubUserId);
		if (existingConnection.isPresent()) {
			GitHubConnection connection = existingConnection.get();
			connection.getUser().updateProfile(email, displayName);
			connection.updateCredentials(login, avatarUrl, encryptedToken, String.join(",", scopes));
			return connection;
		}

		User user = userRepository.findByEmailIgnoreCase(email)
				.orElseGet(() -> userRepository.save(new User(email, displayName)));
		return connectionRepository.save(new GitHubConnection(
				user, githubUserId, login, avatarUrl, encryptedToken, String.join(",", scopes)));
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<GitHubConnection> findByGithubUserId(Long githubUserId) {
		return connectionRepository.findByGithubUserId(githubUserId);
	}

	@Override
	@Transactional
	public void disconnect(Long githubUserId) {
		connectionRepository.findByGithubUserId(githubUserId).ifPresent(connectionRepository::delete);
	}

	private Long requiredLong(OAuth2User principal, String attribute) {
		Object value = principal.getAttribute(attribute);
		if (value instanceof Number number) return number.longValue();
		throw new IllegalArgumentException("GitHub did not provide the required " + attribute + " attribute.");
	}

	private String requiredString(OAuth2User principal, String attribute) {
		String value = principal.getAttribute(attribute);
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException("GitHub did not provide the required " + attribute + " attribute.");
		}
		return value;
	}

	private String optionalString(OAuth2User principal, String attribute, String fallback) {
		String value = principal.getAttribute(attribute);
		return value == null || value.isBlank() ? fallback : value;
	}
}