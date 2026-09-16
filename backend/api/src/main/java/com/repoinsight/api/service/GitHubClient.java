package com.repoinsight.api.service;

public interface GitHubClient {

	GitHubRepositoryPage findRepositories(String accessToken, int page, int perPage);

	GitHubRepository getRepository(String accessToken, long repositoryId);
}