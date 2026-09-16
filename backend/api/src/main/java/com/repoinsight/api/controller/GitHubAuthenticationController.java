package com.repoinsight.api.controller;

import java.net.URI;

import com.repoinsight.api.configuration.AppProperties;
import com.repoinsight.api.dto.auth.CsrfTokenResponse;
import com.repoinsight.api.dto.auth.GitHubUserResponse;
import com.repoinsight.api.service.GitHubAuthenticationService;
import com.repoinsight.api.service.exception.GitHubOAuthNotConfiguredException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class GitHubAuthenticationController {

	private final GitHubAuthenticationService authenticationService;
	private final OAuth2AuthorizedClientService authorizedClientService;
	private final AppProperties appProperties;

	public GitHubAuthenticationController(
			GitHubAuthenticationService authenticationService,
			OAuth2AuthorizedClientService authorizedClientService,
			AppProperties appProperties) {
		this.authenticationService = authenticationService;
		this.authorizedClientService = authorizedClientService;
		this.appProperties = appProperties;
	}

	@GetMapping("/github/start")
	public ResponseEntity<Void> start() {
		if (!appProperties.github().configured()) {
			throw new GitHubOAuthNotConfiguredException();
		}
		return ResponseEntity.status(302).location(URI.create("/oauth2/authorization/github")).build();
	}

	@GetMapping("/github/me")
	public GitHubUserResponse current(Authentication authentication) {
		if (!(authentication instanceof OAuth2AuthenticationToken oauth) || !authentication.isAuthenticated()) {
			return GitHubUserResponse.disconnected(appProperties.github().configured());
		}
		Long githubUserId = githubUserId(oauth);
		return authenticationService.findByGithubUserId(githubUserId)
				.map(GitHubUserResponse::connected)
				.orElseGet(() -> GitHubUserResponse.disconnected(appProperties.github().configured()));
	}

	@GetMapping("/csrf")
	public CsrfTokenResponse csrf(CsrfToken csrfToken) {
		return CsrfTokenResponse.from(csrfToken);
	}

	@PostMapping("/github/disconnect")
	public ResponseEntity<Void> disconnect(
			OAuth2AuthenticationToken authentication,
			HttpServletRequest request) throws ServletException {
		authenticationService.disconnect(githubUserId(authentication));
		authorizedClientService.removeAuthorizedClient(
				authentication.getAuthorizedClientRegistrationId(), authentication.getName());
		request.logout();
		return ResponseEntity.noContent().build();
	}

	private Long githubUserId(OAuth2AuthenticationToken authentication) {
		Object id = authentication.getPrincipal().getAttribute("id");
		if (id instanceof Number number) return number.longValue();
		throw new IllegalArgumentException("GitHub did not provide the required id attribute.");
	}
}