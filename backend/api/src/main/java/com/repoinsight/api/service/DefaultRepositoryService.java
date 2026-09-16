package com.repoinsight.api.service;

import java.util.List;
import java.util.UUID;

import com.repoinsight.api.domain.AnalysisJob;
import com.repoinsight.api.domain.Repository;
import com.repoinsight.api.repository.AnalysisJobRepository;
import com.repoinsight.api.repository.RepositoryRepository;
import com.repoinsight.api.service.exception.RepositoryAlreadyConnectedException;
import com.repoinsight.api.service.exception.RepositoryNotFoundException;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DefaultRepositoryService implements RepositoryService {

	private final RepositoryRepository repositoryRepository;
	private final AnalysisJobRepository analysisJobRepository;
	private final AnalysisJobQueue analysisJobQueue;
	private final GitHubRepositoryUrlParser urlParser;

	public DefaultRepositoryService(
			RepositoryRepository repositoryRepository,
			AnalysisJobRepository analysisJobRepository,
			AnalysisJobQueue analysisJobQueue,
			GitHubRepositoryUrlParser urlParser) {
		this.repositoryRepository = repositoryRepository;
		this.analysisJobRepository = analysisJobRepository;
		this.analysisJobQueue = analysisJobQueue;
		this.urlParser = urlParser;
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "repositories-v2", key = "'all'")
	public List<Repository> findAll() {
		return repositoryRepository.findAll();
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "repository-v2", key = "#id")
	public Repository findById(UUID id) {
		return repositoryRepository.findById(id).orElseThrow(() -> new RepositoryNotFoundException(id));
	}

	@Override
	@Transactional
	@CacheEvict(value = { "repositories-v2", "repository-v2" }, allEntries = true)
	public Repository create(String githubUrl) {
		GitHubRepositoryCoordinates coordinates = urlParser.parse(githubUrl);
		ensureAvailable(coordinates.fullName(), null);

		Repository repository = repositoryRepository.save(
				new Repository(coordinates.owner(), coordinates.name(), coordinates.canonicalUrl()));
		AnalysisJob analysisJob = analysisJobRepository.save(new AnalysisJob(repository));
		analysisJobQueue.enqueue(analysisJob.getId());
		return repository;
	}

	@Override
	@Transactional
	@CacheEvict(value = { "repositories-v2", "repository-v2" }, allEntries = true)
	public Repository update(UUID id, String githubUrl) {
		Repository repository = repositoryRepository.findById(id)
				.orElseThrow(() -> new RepositoryNotFoundException(id));
		GitHubRepositoryCoordinates coordinates = urlParser.parse(githubUrl);
		ensureAvailable(coordinates.fullName(), id);
		repository.update(coordinates.owner(), coordinates.name(), coordinates.canonicalUrl());
		return repository;
	}

	@Override
	@Transactional
	@CacheEvict(value = { "repositories-v2", "repository-v2" }, allEntries = true)
	public void delete(UUID id) {
		Repository repository = repositoryRepository.findById(id)
				.orElseThrow(() -> new RepositoryNotFoundException(id));
		repositoryRepository.delete(repository);
	}

	private void ensureAvailable(String fullName, UUID currentId) {
		boolean exists = currentId == null
				? repositoryRepository.existsByFullNameIgnoreCase(fullName)
				: repositoryRepository.existsByFullNameIgnoreCaseAndIdNot(fullName, currentId);
		if (exists) {
			throw new RepositoryAlreadyConnectedException(fullName);
		}
	}
}