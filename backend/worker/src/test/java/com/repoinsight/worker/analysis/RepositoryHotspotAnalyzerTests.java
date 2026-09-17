package com.repoinsight.worker.analysis;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class RepositoryHotspotAnalyzerTests {

	private final RepositoryHotspotAnalyzer analyzer = new RepositoryHotspotAnalyzer();

	@Test
	void normalizesAndCombinesAllSixFactorsDeterministically() {
		StructuralFileMetrics risky = structure("src/main/java/app/Risky.java", 20, List.of());
		StructuralFileMetrics dependency = structure(
				"src/main/java/app/Dependency.java", 5, List.of("import app.Risky;"));
		GitHistoryAnalysisResult history = history(
				List.of(history(risky.filePath(), 1000, 90, 8, 30), history(dependency.filePath(), 10, 40, 0, 2)),
				List.of(history(risky.filePath(), 300, 100, 4, 12), history(dependency.filePath(), 5, 100, 0, 1)));

		List<FileHotspotMetric> result = analyzer.analyze(List.of(risky, dependency), history);

		assertThat(result).extracting(FileHotspotMetric::filePath)
				.containsExactly(risky.filePath(), dependency.filePath());
		assertThat(result.get(0)).satisfies(metric -> {
			assertThat(metric.normalizedChurn()).isEqualTo(1);
			assertThat(metric.normalizedComplexity()).isEqualTo(1);
			assertThat(metric.normalizedContributorConcentration()).isEqualTo(0.9);
			assertThat(metric.normalizedBugFixActivity()).isEqualTo(1);
			assertThat(metric.normalizedModificationFrequency()).isEqualTo(1);
			assertThat(metric.dependencyReferences()).isEqualTo(1);
			assertThat(metric.normalizedDependencyImportance()).isEqualTo(1);
			assertThat(metric.riskScore()).isEqualTo(98.5);
			assertThat(metric.riskLevel()).isEqualTo(HotspotRiskLevel.CRITICAL);
		});
		assertThat(result.get(1).riskLevel()).isEqualTo(HotspotRiskLevel.MEDIUM);
	}

	@Test
	void returnsNoScoresWithoutGitHistory() {
		assertThat(analyzer.analyze(List.of(structure("src/App.java", 3, List.of())), GitHistoryAnalysisResult.empty()))
				.isEmpty();
	}

	@Test
	void classifiesEveryRiskBoundary() {
		assertThat(RepositoryHotspotAnalyzer.riskLevel(0)).isEqualTo(HotspotRiskLevel.LOW);
		assertThat(RepositoryHotspotAnalyzer.riskLevel(24.9)).isEqualTo(HotspotRiskLevel.LOW);
		assertThat(RepositoryHotspotAnalyzer.riskLevel(25)).isEqualTo(HotspotRiskLevel.MEDIUM);
		assertThat(RepositoryHotspotAnalyzer.riskLevel(49.9)).isEqualTo(HotspotRiskLevel.MEDIUM);
		assertThat(RepositoryHotspotAnalyzer.riskLevel(50)).isEqualTo(HotspotRiskLevel.HIGH);
		assertThat(RepositoryHotspotAnalyzer.riskLevel(74.9)).isEqualTo(HotspotRiskLevel.HIGH);
		assertThat(RepositoryHotspotAnalyzer.riskLevel(75)).isEqualTo(HotspotRiskLevel.CRITICAL);
		assertThat(RepositoryHotspotAnalyzer.riskLevel(100)).isEqualTo(HotspotRiskLevel.CRITICAL);
	}

	private StructuralFileMetrics structure(String path, int complexity, List<String> imports) {
		return new StructuralFileMetrics(path, "Java", 1, 0, 1, 0, imports, 5, 5, 1, 1,
				complexity, complexity, false, List.of());
	}

	private FileHistoryMetric history(String path, int churn, double ownership, int fixes, int commits) {
		return new FileHistoryMetric(path, "Java", commits, churn, 0, 1, fixes, Instant.parse("2026-09-15T00:00:00Z"),
				"Maintainer", ownership, 1, ownership >= 70);
	}

	private GitHistoryAnalysisResult history(List<FileHistoryMetric> all, List<FileHistoryMetric> recent) {
		return new GitHistoryAnalysisResult(
				Map.of(HistoryPeriod.ALL, all, HistoryPeriod.DAYS_90, recent), List.of(), List.of());
	}
}