package com.repoinsight.api.service;

import java.util.List;
import java.util.UUID;

import com.repoinsight.api.domain.Repository;

public interface RepositoryService {

	List<Repository> findAll();

	Repository findById(UUID id);

	Repository create(String githubUrl);

	Repository update(UUID id, String githubUrl);

	void delete(UUID id);
}