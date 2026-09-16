package com.repoinsight.worker.analysis;

import java.util.List;

public record StructuralFileMetrics(
		String filePath,
		String language,
		int classCount,
		int interfaceCount,
		int methodCount,
		int functionCount,
		List<String> imports,
		double averageMethodLength,
		int maximumMethodLength,
		int maximumNestingDepth,
		int controlFlowCount,
		boolean parseError,
		List<StructuralSymbol> symbols) {
}