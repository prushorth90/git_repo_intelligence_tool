package com.repoinsight.api.repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import com.repoinsight.api.domain.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RepositoryRepository extends JpaRepository<Repository, UUID> {

	boolean existsByFullNameIgnoreCase(String fullName);

	boolean existsByFullNameIgnoreCaseAndIdNot(String fullName, UUID id);

	boolean existsByGithubRepositoryId(Long githubRepositoryId);

	List<Repository> findAllByGithubRepositoryIdIn(Collection<Long> githubRepositoryIds);
}