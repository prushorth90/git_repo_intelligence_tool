package com.repoinsight.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;
import java.util.Optional;

import com.repoinsight.api.domain.AnalysisJob;
import com.repoinsight.api.domain.AnalysisJobStatus;
import com.repoinsight.api.domain.Repository;
import com.repoinsight.api.repository.AnalysisJobRepository;
import com.repoinsight.api.repository.RepositoryRepository;
import com.repoinsight.api.service.exception.RepositoryNotFoundException;

import org.junit.jupiter.api.Test;

class DefaultAnalysisHistoryServiceTests {

	private final RepositoryRepository repositoryRepository = mock(RepositoryRepository.class);
	private final AnalysisJobRepository analysisJobRepository = mock(AnalysisJobRepository.class);
	private final AnalysisJobQueue analysisJobQueue = mock(AnalysisJobQueue.class);
	private final DefaultAnalysisHistoryService service =
			new DefaultAnalysisHistoryService(repositoryRepository, analysisJobRepository, analysisJobQueue);

	@Test
	void rejectsUnknownRepository() {
		UUID repositoryId = UUID.randomUUID();
		when(repositoryRepository.existsById(repositoryId)).thenReturn(false);

		assertThatThrownBy(() -> service.findByRepositoryId(repositoryId))
				.isInstanceOf(RepositoryNotFoundException.class);
		verify(analysisJobRepository, never()).findByRepositoryIdOrderByRequestedAtDesc(repositoryId);
	}

	@Test
	void persistsAndPublishesQueuedAnalysis() {
		UUID repositoryId = UUID.randomUUID();
		Repository repository = new Repository("owner", "repository", "https://github.com/owner/repository");
		when(repositoryRepository.findById(repositoryId)).thenReturn(Optional.of(repository));
		when(analysisJobRepository.save(any(AnalysisJob.class))).thenAnswer(invocation -> invocation.getArgument(0));

		AnalysisJob job = service.requestAnalysis(repositoryId, false);

		assertThat(job.getStatus()).isEqualTo(AnalysisJobStatus.QUEUED);
		assertThat(job.getProgressPercentage()).isZero();
		verify(analysisJobRepository).save(job);
		verify(analysisJobQueue).enqueue(job.getId());
	}
}