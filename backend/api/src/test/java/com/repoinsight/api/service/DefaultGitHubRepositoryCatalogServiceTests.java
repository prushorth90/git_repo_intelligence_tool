package com.repoinsight.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;

import com.repoinsight.api.domain.AnalysisJob;
import com.repoinsight.api.domain.GitHubConnection;
import com.repoinsight.api.domain.Repository;
import com.repoinsight.api.domain.User;
import com.repoinsight.api.infrastructure.security.AccessTokenCipher;
import com.repoinsight.api.repository.AnalysisJobRepository;
import com.repoinsight.api.repository.GitHubConnectionRepository;
import com.repoinsight.api.repository.RepositoryRepository;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class DefaultGitHubRepositoryCatalogServiceTests {

	private final GitHubConnectionRepository connectionRepository = mock(GitHubConnectionRepository.class);
	private final RepositoryRepository repositoryRepository = mock(RepositoryRepository.class);
	private final AnalysisJobRepository analysisJobRepository = mock(AnalysisJobRepository.class);
	private final AnalysisJobQueue analysisJobQueue = mock(AnalysisJobQueue.class);
	private final GitHubClient gitHubClient = mock(GitHubClient.class);
	private final AccessTokenCipher accessTokenCipher = mock(AccessTokenCipher.class);
	private final DefaultGitHubRepositoryCatalogService service = new DefaultGitHubRepositoryCatalogService(
			connectionRepository, repositoryRepository, analysisJobRepository, analysisJobQueue, gitHubClient, accessTokenCipher);

	@Test
	void importsTrustedGitHubMetadataAndQueuesAnalysis() {
		long githubUserId = 42L;
		GitHubConnection connection = new GitHubConnection(
				new User("octocat@example.com", "Octocat"), githubUserId, "octocat", "avatar", "encrypted", "repo");
		GitHubRepository remote = new GitHubRepository(
				99L, "octocat", "hello-world", "octocat/hello-world", "https://github.com/octocat/hello-world",
				"trunk", "private", true, "Java", 120, 14, Instant.parse("2026-09-01T10:15:30Z"));
		when(connectionRepository.findByGithubUserId(githubUserId)).thenReturn(Optional.of(connection));
		when(accessTokenCipher.decrypt("encrypted")).thenReturn("token");
		when(gitHubClient.getRepository("token", 99L)).thenReturn(remote);
		when(repositoryRepository.save(any(Repository.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(analysisJobRepository.save(any(AnalysisJob.class))).thenAnswer(invocation -> invocation.getArgument(0));

		Repository imported = service.importRepository(githubUserId, 99L);

		assertThat(imported.getGithubRepositoryId()).isEqualTo(99L);
		assertThat(imported.getFullName()).isEqualTo("octocat/hello-world");
		assertThat(imported.getDefaultBranch()).isEqualTo("trunk");
		assertThat(imported.getVisibility()).isEqualTo("private");
		assertThat(imported.getPrimaryLanguage()).isEqualTo("Java");
		assertThat(imported.getStars()).isEqualTo(120);
		assertThat(imported.getForks()).isEqualTo(14);
		assertThat(imported.getGithubUpdatedAt()).isEqualTo(Instant.parse("2026-09-01T10:15:30Z"));
		assertThat(imported.getGithubConnection()).isSameAs(connection);
		verify(analysisJobRepository).save(any(AnalysisJob.class));
		ArgumentCaptor<Repository> repositoryCaptor = ArgumentCaptor.forClass(Repository.class);
		verify(repositoryRepository).save(repositoryCaptor.capture());
		assertThat(repositoryCaptor.getValue().getGithubUrl()).isEqualTo("https://github.com/octocat/hello-world");
	}
}