package com.repoinsight.worker.analysis;

import java.util.List;
import java.util.Map;

public record GitHistoryAnalysisResult(Map<HistoryPeriod, List<FileHistoryMetric>> metricsByPeriod) {

	public static GitHistoryAnalysisResult empty() {
		return new GitHistoryAnalysisResult(Map.of());
	}
}