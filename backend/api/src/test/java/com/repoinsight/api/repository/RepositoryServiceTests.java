package com.repoinsight.api.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.repoinsight.api.analysis.AnalysisJobPublisher;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class RepositoryServiceTests {

	private final RepositoryService service = new RepositoryService(
			Mockito.mock(ConnectedRepositoryRepository.class), Mockito.mock(AnalysisJobPublisher.class));

	@Test
	void parsesAndNormalizesGithubUrl() {
		RepositoryService.RepositoryCoordinates result = service.parse("https://github.com/spring-projects/spring-boot.git");

		assertThat(result.owner()).isEqualTo("spring-projects");
		assertThat(result.name()).isEqualTo("spring-boot");
		assertThat(result.canonicalUrl()).isEqualTo("https://github.com/spring-projects/spring-boot");
	}

	@Test
	void rejectsNonGithubUrl() {
		assertThatThrownBy(() -> service.parse("https://example.com/owner/repository"))
				.isInstanceOf(InvalidRepositoryUrlException.class);
	}
}