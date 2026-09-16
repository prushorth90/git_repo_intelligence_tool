package com.repoinsight.worker.analysis;

import java.util.List;
import java.util.Map;

public record SourceAnalysisResult(
		int sourceFileCount,
		long repositorySizeBytes,
		Map<String, Integer> languageDistribution,
		List<String> directoryStructure,
		Map<String, Integer> fileExtensions) {
}