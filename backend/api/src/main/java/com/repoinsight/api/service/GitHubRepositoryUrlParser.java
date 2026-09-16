package com.repoinsight.api.service;

import java.net.URI;

import com.repoinsight.api.service.exception.InvalidRepositoryUrlException;

import org.springframework.stereotype.Component;

@Component
public class GitHubRepositoryUrlParser {

	public GitHubRepositoryCoordinates parse(String value) {
		try {
			URI uri = URI.create(value.trim());
			if (!"https".equalsIgnoreCase(uri.getScheme()) || !"github.com".equalsIgnoreCase(uri.getHost())) {
				throw new IllegalArgumentException("Repository URL must use https://github.com.");
			}

			String path = uri.getPath().replaceFirst("^/", "").replaceFirst("\\.git/?$", "");
			String[] segments = path.split("/");
			if (segments.length != 2 || segments[0].isBlank() || segments[1].isBlank()) {
				throw new IllegalArgumentException("Repository URL must identify an owner and repository.");
			}

			return new GitHubRepositoryCoordinates(
					segments[0], segments[1], "https://github.com/" + segments[0] + "/" + segments[1]);
		} catch (IllegalArgumentException exception) {
			throw new InvalidRepositoryUrlException(exception.getMessage());
		}
	}
}