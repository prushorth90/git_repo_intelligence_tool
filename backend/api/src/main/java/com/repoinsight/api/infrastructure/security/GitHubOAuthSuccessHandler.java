package com.repoinsight.api.infrastructure.security;

import java.io.IOException;

import com.repoinsight.api.configuration.AppProperties;
import com.repoinsight.api.service.GitHubAuthenticationService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class GitHubOAuthSuccessHandler implements AuthenticationSuccessHandler {

	private static final Logger logger = LoggerFactory.getLogger(GitHubOAuthSuccessHandler.class);

	private final OAuth2AuthorizedClientService authorizedClientService;
	private final GitHubAuthenticationService authenticationService;
	private final AppProperties appProperties;

	public GitHubOAuthSuccessHandler(
			OAuth2AuthorizedClientService authorizedClientService,
			GitHubAuthenticationService authenticationService,
			AppProperties appProperties) {
		this.authorizedClientService = authorizedClientService;
		this.authenticationService = authenticationService;
		this.appProperties = appProperties;
	}

	@Override
	public void onAuthenticationSuccess(
			HttpServletRequest request,
			HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {
		try {
			OAuth2AuthenticationToken oauth = (OAuth2AuthenticationToken) authentication;
			OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(
					oauth.getAuthorizedClientRegistrationId(), oauth.getName());
			if (authorizedClient == null) {
				throw new IllegalStateException("The GitHub authorized client was not available.");
			}
			authenticationService.connect(
					(OAuth2User) oauth.getPrincipal(),
					authorizedClient.getAccessToken().getTokenValue(),
					authorizedClient.getAccessToken().getScopes());
			response.sendRedirect(frontendRedirect("connected"));
		} catch (RuntimeException exception) {
			logger.error("GitHub OAuth login could not be persisted", exception);
			response.sendRedirect(frontendRedirect("error"));
		}
	}

	private String frontendRedirect(String status) {
		return UriComponentsBuilder.fromUriString(appProperties.github().frontendRedirectUrl())
				.queryParam("github", status)
				.build()
				.toUriString();
	}
}