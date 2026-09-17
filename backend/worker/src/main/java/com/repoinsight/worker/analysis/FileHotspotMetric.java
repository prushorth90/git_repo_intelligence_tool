package com.repoinsight.worker.analysis;

public record FileHotspotMetric(
		String filePath,
		String language,
		long churn,
		int complexity,
		double contributorConcentration,
		int bugFixCommits,
		int dependencyReferences,
		int recentModifications,
		double normalizedChurn,
		double normalizedComplexity,
		double normalizedContributorConcentration,
		double normalizedBugFixActivity,
		double normalizedDependencyImportance,
		double normalizedModificationFrequency,
		double riskScore,
		HotspotRiskLevel riskLevel) {
}