package com.repoinsight.api.dto.analysis;

import com.repoinsight.api.domain.MethodComplexityMetric;

public record MethodComplexityResponse(
		String filePath,
		String language,
		String symbolName,
		String symbolKind,
		int startLine,
		int endLine,
		int lineCount,
		int nestingDepth,
		int cyclomaticComplexity) {

	public static MethodComplexityResponse from(MethodComplexityMetric metric) {
		return new MethodComplexityResponse(
				metric.getFilePath(), metric.getLanguage(), metric.getSymbolName(), metric.getSymbolKind(),
				metric.getStartLine(), metric.getEndLine(), metric.getLineCount(), metric.getNestingDepth(),
				metric.getCyclomaticComplexity());
	}
}