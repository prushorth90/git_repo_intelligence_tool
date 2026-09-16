package com.repoinsight.api.service;

public record GitHubRepositoryCoordinates(String owner, String name, String canonicalUrl) {

	public String fullName() {
		return owner + "/" + name;
	}
}