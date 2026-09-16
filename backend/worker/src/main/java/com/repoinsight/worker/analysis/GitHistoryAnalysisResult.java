package com.repoinsight.worker.analysis;

import java.util.List;
import java.util.Map;

public record GitHistoryAnalysisResult(
		Map<HistoryPeriod, List<FileHistoryMetric>> metricsByPeriod,
		List<FileOwnershipResult> fileOwnership,
		List<ContributorOwnershipResult> contributors) {

	public static GitHistoryAnalysisResult empty() {
		return new GitHistoryAnalysisResult(Map.of(), List.of(), List.of());
	}
}