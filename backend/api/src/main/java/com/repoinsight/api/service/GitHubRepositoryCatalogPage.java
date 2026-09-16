package com.repoinsight.api.service;

import java.util.Set;

public record GitHubRepositoryCatalogPage(GitHubRepositoryPage page, Set<Long> importedRepositoryIds) {
}