package com.repoinsight.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.repoinsight.api.domain.MethodComplexityMetric;
import com.repoinsight.api.domain.RepositoryAnalysis;
import com.repoinsight.api.domain.SourceStructureMetric;
import com.repoinsight.api.repository.MethodComplexityMetricRepository;
import com.repoinsight.api.repository.RepositoryAnalysisRepository;
import com.repoinsight.api.repository.RepositoryRepository;
import com.repoinsight.api.repository.SourceStructureMetricRepository;

import org.junit.jupiter.api.Test;

class DefaultComplexityServiceTests {

	private final RepositoryRepository repositoryRepository = mock(RepositoryRepository.class);
	private final RepositoryAnalysisRepository analysisRepository = mock(RepositoryAnalysisRepository.class);
	private final SourceStructureMetricRepository fileRepository = mock(SourceStructureMetricRepository.class);
	private final MethodComplexityMetricRepository methodRepository = mock(MethodComplexityMetricRepository.class);
	private final DefaultComplexityService service = new DefaultComplexityService(
			repositoryRepository, analysisRepository, fileRepository, methodRepository);

	@Test
	void returnsRankedComplexityFromLatestAnalysis() {
		UUID repositoryId = UUID.randomUUID();
		UUID analysisId = UUID.randomUUID();
		Instant analyzedAt = Instant.parse("2026-09-16T05:00:00Z");
		RepositoryAnalysis analysis = mock(RepositoryAnalysis.class);
		SourceStructureMetric file = mock(SourceStructureMetric.class);
		MethodComplexityMetric method = mock(MethodComplexityMetric.class);

		when(repositoryRepository.existsById(repositoryId)).thenReturn(true);
		when(analysisRepository.findFirstByRepositoryIdOrderByAnalyzedAtDesc(repositoryId)).thenReturn(Optional.of(analysis));
		when(analysis.getId()).thenReturn(analysisId);
		when(analysis.getAnalyzedAt()).thenReturn(analyzedAt);
		when(file.getFilePath()).thenReturn("src/Parser.java");
		when(file.getLanguage()).thenReturn("Java");
		when(file.getCyclomaticComplexity()).thenReturn(12);
		when(method.getFilePath()).thenReturn("src/Parser.java");
		when(method.getLanguage()).thenReturn("Java");
		when(method.getSymbolName()).thenReturn("parse");
		when(method.getSymbolKind()).thenReturn("method");
		when(method.getCyclomaticComplexity()).thenReturn(8);
		when(fileRepository.findTop100ByAnalysisIdAndParseErrorFalseOrderByCyclomaticComplexityDesc(analysisId))
				.thenReturn(List.of(file));
		when(methodRepository.findTop100ByAnalysisIdOrderByCyclomaticComplexityDesc(analysisId)).thenReturn(List.of(method));

		var response = service.overview(repositoryId);

		assertThat(response.analysisId()).isEqualTo(analysisId);
		assertThat(response.analyzedAt()).isEqualTo(analyzedAt);
		assertThat(response.scoringFormula()).isEqualTo("Cyclomatic complexity = 1 + decision points");
		assertThat(response.decisionRules()).hasSize(7);
		assertThat(response.files()).singleElement().satisfies(result -> {
			assertThat(result.filePath()).isEqualTo("src/Parser.java");
			assertThat(result.cyclomaticComplexity()).isEqualTo(12);
		});
		assertThat(response.methods()).singleElement().satisfies(result -> {
			assertThat(result.symbolName()).isEqualTo("parse");
			assertThat(result.cyclomaticComplexity()).isEqualTo(8);
		});
	}

	@Test
	void returnsScoringRulesAndEmptyRankingsBeforeFirstAnalysis() {
		UUID repositoryId = UUID.randomUUID();
		when(repositoryRepository.existsById(repositoryId)).thenReturn(true);
		when(analysisRepository.findFirstByRepositoryIdOrderByAnalyzedAtDesc(repositoryId)).thenReturn(Optional.empty());

		var response = service.overview(repositoryId);

		assertThat(response.analysisId()).isNull();
		assertThat(response.files()).isEmpty();
		assertThat(response.methods()).isEmpty();
		assertThat(response.decisionRules()).isNotEmpty();
	}
}