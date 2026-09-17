package com.repoinsight.api.dto.analysis;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import com.repoinsight.api.domain.FileHotspotMetric;
import com.repoinsight.api.domain.HotspotRiskLevel;

public record FileHotspotResponse(
		String filePath,
		String language,
		double riskScore,
		HotspotRiskLevel riskLevel,
		List<HotspotFactorResponse> factors,
		List<String> reasons) {

	public static FileHotspotResponse from(FileHotspotMetric metric) {
		List<HotspotFactorResponse> factors = List.of(
				factor("churn", "Code churn", metric.getChurn(), metric.getNormalizedChurn(), 0.25,
						metric.getChurn() + " changed lines"),
				factor("complexity", "Complexity", metric.getComplexity(), metric.getNormalizedComplexity(), 0.20,
						"Cyclomatic complexity " + metric.getComplexity()),
				factor("concentration", "Contributor concentration", metric.getContributorConcentration(),
						metric.getNormalizedContributorConcentration(), 0.15,
						format(metric.getContributorConcentration()) + "% top-owner share"),
				factor("bugFixes", "Recent bug fixes", metric.getBugFixCommits(), metric.getNormalizedBugFixActivity(), 0.20,
						metric.getBugFixCommits() + " bug-fix commits in 90 days"),
				factor("dependencies", "Dependency importance", metric.getDependencyReferences(),
						metric.getNormalizedDependencyImportance(), 0.10,
						metric.getDependencyReferences() + " inbound file imports"),
				factor("modifications", "Modification frequency", metric.getRecentModifications(),
						metric.getNormalizedModificationFrequency(), 0.10,
						metric.getRecentModifications() + " modifications in 90 days"));
		List<String> reasons = factors.stream()
				.filter(factor -> factor.contribution() > 0)
				.sorted(Comparator.comparingDouble(HotspotFactorResponse::contribution).reversed()
						.thenComparing(HotspotFactorResponse::key))
				.limit(3)
				.map(factor -> factor.label() + " contributed " + format(factor.contribution())
						+ " points (" + factor.explanation() + ").")
				.toList();
		return new FileHotspotResponse(metric.getFilePath(), metric.getLanguage(), metric.getRiskScore(),
				metric.getRiskLevel(), factors, reasons);
	}

	private static HotspotFactorResponse factor(
			String key, String label, double rawValue, double normalizedValue, double weight, String explanation) {
		return new HotspotFactorResponse(key, label, rawValue, normalizedValue, weight,
				Math.round(normalizedValue * weight * 1000.0) / 10.0, explanation);
	}

	private static String format(double value) {
		return String.format(Locale.ROOT, "%.1f", value);
	}
}