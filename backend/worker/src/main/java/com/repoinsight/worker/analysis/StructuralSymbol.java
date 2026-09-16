package com.repoinsight.worker.analysis;

public record StructuralSymbol(
		String name,
		String kind,
		int startLine,
		int endLine,
		int length,
		int nestingDepth,
		int cyclomaticComplexity) {
}