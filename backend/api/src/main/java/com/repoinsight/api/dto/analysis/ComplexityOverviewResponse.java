package com.repoinsight.api.dto.analysis;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ComplexityOverviewResponse(
		UUID analysisId,
		Instant analyzedAt,
		String scoringFormula,
		List<String> decisionRules,
		List<FileComplexityResponse> files,
		List<MethodComplexityResponse> methods) {

	public static ComplexityOverviewResponse empty(String formula, List<String> rules) {
		return new ComplexityOverviewResponse(null, null, formula, rules, List.of(), List.of());
	}
}