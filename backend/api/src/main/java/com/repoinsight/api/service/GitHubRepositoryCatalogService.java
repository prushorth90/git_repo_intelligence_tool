package com.repoinsight.api.service;

import com.repoinsight.api.domain.Repository;

public interface GitHubRepositoryCatalogService {

	GitHubRepositoryCatalogPage findRepositories(long githubUserId, int page, int perPage);

	Repository importRepository(long githubUserId, long githubRepositoryId);
}