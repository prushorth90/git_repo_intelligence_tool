package com.repoinsight.api.infrastructure.security;

import java.io.IOException;

import com.repoinsight.api.configuration.AppProperties;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class GitHubOAuthFailureHandler implements AuthenticationFailureHandler {

	private final AppProperties appProperties;

	public GitHubOAuthFailureHandler(AppProperties appProperties) {
		this.appProperties = appProperties;
	}

	@Override
	public void onAuthenticationFailure(
			HttpServletRequest request,
			HttpServletResponse response,
			AuthenticationException exception) throws IOException, ServletException {
		String redirect = UriComponentsBuilder.fromUriString(appProperties.github().frontendRedirectUrl())
				.queryParam("github", "error")
				.build()
				.toUriString();
		response.sendRedirect(redirect);
	}
}