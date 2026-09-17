package com.repoinsight.api.dto.analysis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.repoinsight.api.domain.FileHotspotMetric;
import com.repoinsight.api.domain.HotspotRiskLevel;

import org.junit.jupiter.api.Test;

class FileHotspotResponseTests {

	@Test
	void exposesAllFactorsAndRanksExplanationsByPointContribution() {
		FileHotspotMetric metric = mock(FileHotspotMetric.class);
		when(metric.getFilePath()).thenReturn("src/CheckoutService.java");
		when(metric.getLanguage()).thenReturn("Java");
		when(metric.getRiskScore()).thenReturn(72.5);
		when(metric.getRiskLevel()).thenReturn(HotspotRiskLevel.HIGH);
		when(metric.getChurn()).thenReturn(400L);
		when(metric.getComplexity()).thenReturn(18);
		when(metric.getContributorConcentration()).thenReturn(80.0);
		when(metric.getBugFixCommits()).thenReturn(5);
		when(metric.getDependencyReferences()).thenReturn(9);
		when(metric.getRecentModifications()).thenReturn(12);
		when(metric.getNormalizedChurn()).thenReturn(1.0);
		when(metric.getNormalizedComplexity()).thenReturn(0.5);
		when(metric.getNormalizedContributorConcentration()).thenReturn(0.8);
		when(metric.getNormalizedBugFixActivity()).thenReturn(0.75);
		when(metric.getNormalizedDependencyImportance()).thenReturn(0.4);
		when(metric.getNormalizedModificationFrequency()).thenReturn(0.6);

		FileHotspotResponse response = FileHotspotResponse.from(metric);

		assertThat(response.factors()).hasSize(6);
		assertThat(response.factors()).extracting(HotspotFactorResponse::contribution)
				.containsExactly(25.0, 10.0, 12.0, 15.0, 4.0, 6.0);
		assertThat(response.reasons()).hasSize(3);
		assertThat(response.reasons().get(0)).startsWith("Code churn contributed 25.0 points");
		assertThat(response.reasons().get(1)).startsWith("Recent bug fixes contributed 15.0 points");
		assertThat(response.reasons().get(2)).startsWith("Contributor concentration contributed 12.0 points");
	}
}