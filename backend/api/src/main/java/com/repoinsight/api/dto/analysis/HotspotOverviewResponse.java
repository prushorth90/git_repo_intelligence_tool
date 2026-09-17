package com.repoinsight.api.dto.analysis;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record HotspotOverviewResponse(
		UUID analysisId,
		Instant analyzedAt,
		String formula,
		String normalization,
		String thresholds,
		List<FileHotspotResponse> files) {

	public static HotspotOverviewResponse empty() {
		return new HotspotOverviewResponse(null, null, DefaultScoringRules.FORMULA,
				DefaultScoringRules.NORMALIZATION, DefaultScoringRules.THRESHOLDS, List.of());
	}

	public static final class DefaultScoringRules {
		public static final String FORMULA = "25% churn + 20% complexity + 15% contributor concentration + "
				+ "20% recent bug fixes + 10% dependency importance + 10% recent modification frequency";
		public static final String NORMALIZATION = "Count metrics use log1p(value) / log1p(repository maximum); "
				+ "complexity uses decision points above its baseline of 1; contributor concentration uses top-owner percentage / 100.";
		public static final String THRESHOLDS = "LOW 0-24.9, MEDIUM 25-49.9, HIGH 50-74.9, CRITICAL 75-100";

		private DefaultScoringRules() {
		}
	}
}