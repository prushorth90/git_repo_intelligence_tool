package com.repoinsight.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import com.repoinsight.api.domain.AnalysisJob;
import com.repoinsight.api.domain.Repository;
import com.repoinsight.api.repository.AnalysisJobRepository;
import com.repoinsight.api.repository.RepositoryRepository;
import com.repoinsight.api.service.exception.RepositoryNotFoundException;

import org.junit.jupiter.api.Test;

class DefaultRepositoryServiceTests {

	private final RepositoryRepository repositoryRepository = mock(RepositoryRepository.class);
	private final AnalysisJobRepository analysisJobRepository = mock(AnalysisJobRepository.class);
	private final AnalysisJobQueue analysisJobQueue = mock(AnalysisJobQueue.class);
	private final DefaultRepositoryService service = new DefaultRepositoryService(
			repositoryRepository, analysisJobRepository, analysisJobQueue, new GitHubRepositoryUrlParser());

	@Test
	void createsRepositoryAndQueuesAnalysisJob() {
		when(repositoryRepository.save(any(Repository.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(analysisJobRepository.save(any(AnalysisJob.class))).thenAnswer(invocation -> invocation.getArgument(0));

		Repository result = service.create("https://github.com/spring-projects/spring-boot.git");

		assertThat(result.getFullName()).isEqualTo("spring-projects/spring-boot");
		verify(analysisJobRepository).save(any(AnalysisJob.class));
		verify(analysisJobQueue).enqueue(any());
	}

	@Test
	void updatesRepositoryCoordinates() {
		UUID id = UUID.randomUUID();
		Repository repository = new Repository("old", "name", "https://github.com/old/name");
		when(repositoryRepository.findById(id)).thenReturn(Optional.of(repository));

		Repository result = service.update(id, "https://github.com/new-owner/new-name");

		assertThat(result.getFullName()).isEqualTo("new-owner/new-name");
		assertThat(result.getGithubUrl()).isEqualTo("https://github.com/new-owner/new-name");
	}

	@Test
	void rejectsDeleteForUnknownRepository() {
		UUID id = UUID.randomUUID();
		when(repositoryRepository.findById(id)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.delete(id)).isInstanceOf(RepositoryNotFoundException.class);
		verify(repositoryRepository, never()).delete(any());
	}
}