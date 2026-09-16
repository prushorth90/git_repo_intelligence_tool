package com.repoinsight.api.service;

import java.util.Set;
import java.util.stream.Collectors;

import com.repoinsight.api.domain.AnalysisJob;
import com.repoinsight.api.domain.GitHubConnection;
import com.repoinsight.api.domain.Repository;
import com.repoinsight.api.domain.Repository.GitHubRepositoryMetadata;
import com.repoinsight.api.infrastructure.security.AccessTokenCipher;
import com.repoinsight.api.repository.AnalysisJobRepository;
import com.repoinsight.api.repository.GitHubConnectionRepository;
import com.repoinsight.api.repository.RepositoryRepository;
import com.repoinsight.api.service.exception.GitHubConnectionRequiredException;
import com.repoinsight.api.service.exception.RepositoryAlreadyConnectedException;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DefaultGitHubRepositoryCatalogService implements GitHubRepositoryCatalogService {

	private final GitHubConnectionRepository connectionRepository;
	private final RepositoryRepository repositoryRepository;
	private final AnalysisJobRepository analysisJobRepository;
	private final AnalysisJobQueue analysisJobQueue;
	private final GitHubClient gitHubClient;
	private final AccessTokenCipher accessTokenCipher;

	public DefaultGitHubRepositoryCatalogService(
			GitHubConnectionRepository connectionRepository,
			RepositoryRepository repositoryRepository,
			AnalysisJobRepository analysisJobRepository,
			AnalysisJobQueue analysisJobQueue,
			GitHubClient gitHubClient,
			AccessTokenCipher accessTokenCipher) {
		this.connectionRepository = connectionRepository;
		this.repositoryRepository = repositoryRepository;
		this.analysisJobRepository = analysisJobRepository;
		this.analysisJobQueue = analysisJobQueue;
		this.gitHubClient = gitHubClient;
		this.accessTokenCipher = accessTokenCipher;
	}

	@Override
	@Transactional(readOnly = true)
	public GitHubRepositoryCatalogPage findRepositories(long githubUserId, int page, int perPage) {
		GitHubConnection connection = connection(githubUserId);
		GitHubRepositoryPage result = gitHubClient.findRepositories(
				accessTokenCipher.decrypt(connection.getEncryptedAccessToken()), page, perPage);
		Set<Long> importedIds = repositoryRepository.findAllByGithubRepositoryIdIn(
				result.repositories().stream().map(GitHubRepository::id).toList()).stream()
				.map(Repository::getGithubRepositoryId)
				.collect(Collectors.toSet());
		return new GitHubRepositoryCatalogPage(result, importedIds);
	}

	@Override
	@Transactional
	@CacheEvict(value = { "repositories-v2", "repository-v2" }, allEntries = true)
	public Repository importRepository(long githubUserId, long githubRepositoryId) {
		GitHubConnection connection = connection(githubUserId);
		if (repositoryRepository.existsByGithubRepositoryId(githubRepositoryId)) {
			throw new RepositoryAlreadyConnectedException("GitHub repository " + githubRepositoryId);
		}
		GitHubRepository remote = gitHubClient.getRepository(
				accessTokenCipher.decrypt(connection.getEncryptedAccessToken()), githubRepositoryId);
		if (repositoryRepository.existsByFullNameIgnoreCase(remote.fullName())) {
			throw new RepositoryAlreadyConnectedException(remote.fullName());
		}

		Repository repository = repositoryRepository.save(new Repository(new GitHubRepositoryMetadata(
				remote.id(), remote.owner(), remote.name(), remote.htmlUrl(), remote.defaultBranch(),
				remote.visibility(), remote.privateRepository(), remote.primaryLanguage(), remote.stars(),
				remote.forks(), remote.updatedAt()), connection));
		AnalysisJob job = analysisJobRepository.save(new AnalysisJob(repository));
		analysisJobQueue.enqueue(job.getId());
		return repository;
	}

	private GitHubConnection connection(long githubUserId) {
		return connectionRepository.findByGithubUserId(githubUserId)
				.orElseThrow(GitHubConnectionRequiredException::new);
	}
}