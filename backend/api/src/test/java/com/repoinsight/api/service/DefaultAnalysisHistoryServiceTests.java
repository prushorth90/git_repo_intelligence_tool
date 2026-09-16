package com.repoinsight.api.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import com.repoinsight.api.repository.AnalysisJobRepository;
import com.repoinsight.api.repository.RepositoryRepository;
import com.repoinsight.api.service.exception.RepositoryNotFoundException;

import org.junit.jupiter.api.Test;

class DefaultAnalysisHistoryServiceTests {

	private final RepositoryRepository repositoryRepository = mock(RepositoryRepository.class);
	private final AnalysisJobRepository analysisJobRepository = mock(AnalysisJobRepository.class);
	private final DefaultAnalysisHistoryService service =
			new DefaultAnalysisHistoryService(repositoryRepository, analysisJobRepository);

	@Test
	void rejectsUnknownRepository() {
		UUID repositoryId = UUID.randomUUID();
		when(repositoryRepository.existsById(repositoryId)).thenReturn(false);

		assertThatThrownBy(() -> service.findByRepositoryId(repositoryId))
				.isInstanceOf(RepositoryNotFoundException.class);
		verify(analysisJobRepository, never()).findByRepositoryIdOrderByRequestedAtDesc(repositoryId);
	}
}