package com.repoinsight.api.dto.analysis;

import com.repoinsight.api.domain.SourceStructureMetric;

public record FileComplexityResponse(
		String filePath,
		String language,
		int cyclomaticComplexity,
		int maximumMethodComplexity,
		int methodCount,
		int functionCount,
		int controlFlowCount,
		int maximumNestingDepth) {

	public static FileComplexityResponse from(SourceStructureMetric metric) {
		return new FileComplexityResponse(
				metric.getFilePath(), metric.getLanguage(), metric.getCyclomaticComplexity(),
				metric.getMaximumMethodComplexity(), metric.getMethodCount(), metric.getFunctionCount(),
				metric.getControlFlowCount(), metric.getMaximumNestingDepth());
	}
}