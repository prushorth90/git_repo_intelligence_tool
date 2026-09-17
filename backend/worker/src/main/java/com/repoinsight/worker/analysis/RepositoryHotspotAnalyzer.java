package com.repoinsight.worker.analysis;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

@Component
public class RepositoryHotspotAnalyzer {

	static final double CHURN_WEIGHT = 0.25;
	static final double COMPLEXITY_WEIGHT = 0.20;
	static final double CONCENTRATION_WEIGHT = 0.15;
	static final double BUG_FIX_WEIGHT = 0.20;
	static final double DEPENDENCY_WEIGHT = 0.10;
	static final double MODIFICATION_WEIGHT = 0.10;

	public List<FileHotspotMetric> analyze(
			List<StructuralFileMetrics> structures,
			GitHistoryAnalysisResult history) {
		if (history.metricsByPeriod().isEmpty()) return List.of();

		Map<String, FileHistoryMetric> allTime = byPath(history.metricsByPeriod().getOrDefault(HistoryPeriod.ALL, List.of()));
		Map<String, FileHistoryMetric> recent = byPath(history.metricsByPeriod().getOrDefault(HistoryPeriod.DAYS_90, List.of()));
		Map<String, Integer> dependencyReferences = dependencyReferences(structures);
		List<RawMetric> rawMetrics = structures.stream()
				.filter(metric -> !metric.parseError())
				.map(metric -> {
					FileHistoryMetric lifetime = allTime.get(metric.filePath());
					FileHistoryMetric recentMetric = recent.get(metric.filePath());
					return new RawMetric(
							metric.filePath(), metric.language(), lifetime == null ? 0 : lifetime.totalChurn(),
							metric.cyclomaticComplexity(), lifetime == null ? 0 : lifetime.topOwnershipPercent(),
							recentMetric == null ? 0 : recentMetric.bugFixCommitCount(),
							dependencyReferences.getOrDefault(metric.filePath(), 0),
							recentMetric == null ? 0 : recentMetric.commitCount());
				})
				.toList();

		double maxChurn = rawMetrics.stream().mapToDouble(RawMetric::churn).max().orElse(0);
		double maxComplexity = rawMetrics.stream().mapToDouble(raw -> Math.max(0, raw.complexity() - 1)).max().orElse(0);
		double maxBugFixes = rawMetrics.stream().mapToDouble(RawMetric::bugFixCommits).max().orElse(0);
		double maxDependencies = rawMetrics.stream().mapToDouble(RawMetric::dependencyReferences).max().orElse(0);
		double maxModifications = rawMetrics.stream().mapToDouble(RawMetric::recentModifications).max().orElse(0);

		return rawMetrics.stream().map(raw -> score(raw, maxChurn, maxComplexity, maxBugFixes,
				maxDependencies, maxModifications))
				.sorted(Comparator.comparingDouble(FileHotspotMetric::riskScore).reversed()
						.thenComparing(FileHotspotMetric::filePath))
				.toList();
	}

	private FileHotspotMetric score(
			RawMetric raw,
			double maxChurn,
			double maxComplexity,
			double maxBugFixes,
			double maxDependencies,
			double maxModifications) {
		double churn = normalize(raw.churn(), maxChurn);
		double complexity = normalize(Math.max(0, raw.complexity() - 1), maxComplexity);
		double concentration = clamp(raw.contributorConcentration() / 100.0);
		double bugFixes = normalize(raw.bugFixCommits(), maxBugFixes);
		double dependencies = normalize(raw.dependencyReferences(), maxDependencies);
		double modifications = normalize(raw.recentModifications(), maxModifications);
		double riskScore = round(100.0 * (CHURN_WEIGHT * churn
				+ COMPLEXITY_WEIGHT * complexity
				+ CONCENTRATION_WEIGHT * concentration
				+ BUG_FIX_WEIGHT * bugFixes
				+ DEPENDENCY_WEIGHT * dependencies
				+ MODIFICATION_WEIGHT * modifications));
		return new FileHotspotMetric(
				raw.filePath(), raw.language(), raw.churn(), raw.complexity(), raw.contributorConcentration(),
				raw.bugFixCommits(), raw.dependencyReferences(), raw.recentModifications(),
				round(churn), round(complexity), round(concentration), round(bugFixes), round(dependencies),
				round(modifications), riskScore, riskLevel(riskScore));
	}

	private double normalize(double value, double maximum) {
		if (value <= 0 || maximum <= 0) return 0;
		return Math.log1p(value) / Math.log1p(maximum);
	}

	private double clamp(double value) {
		return Math.max(0, Math.min(1, value));
	}

	private double round(double value) {
		return Math.round(value * 1000.0) / 1000.0;
	}

	static HotspotRiskLevel riskLevel(double score) {
		if (score >= 75) return HotspotRiskLevel.CRITICAL;
		if (score >= 50) return HotspotRiskLevel.HIGH;
		if (score >= 25) return HotspotRiskLevel.MEDIUM;
		return HotspotRiskLevel.LOW;
	}

	private Map<String, FileHistoryMetric> byPath(List<FileHistoryMetric> metrics) {
		Map<String, FileHistoryMetric> byPath = new HashMap<>();
		metrics.forEach(metric -> byPath.put(metric.filePath(), metric));
		return byPath;
	}

	private Map<String, Integer> dependencyReferences(List<StructuralFileMetrics> structures) {
		Map<String, Set<String>> aliasTargets = new HashMap<>();
		Map<String, Integer> basenameCounts = new HashMap<>();
		for (StructuralFileMetrics metric : structures) {
			String path = withoutExtension(metric.filePath()).toLowerCase(Locale.ROOT);
			String basename = basename(path);
			basenameCounts.merge(basename, 1, Integer::sum);
			List<String> segments = List.of(path.split("/"));
			for (int index = 0; index < segments.size() - 1; index++) {
				String alias = String.join("/", segments.subList(index, segments.size()));
				if (alias.contains("/")) aliasTargets.computeIfAbsent(alias, ignored -> new HashSet<>()).add(metric.filePath());
			}
		}
		for (StructuralFileMetrics metric : structures) {
			String path = withoutExtension(metric.filePath()).toLowerCase(Locale.ROOT);
			String basename = basename(path);
			if (basenameCounts.getOrDefault(basename, 0) == 1) {
				aliasTargets.computeIfAbsent(basename, ignored -> new HashSet<>()).add(metric.filePath());
			}
		}

		Map<String, Set<String>> importersByTarget = new HashMap<>();
		for (StructuralFileMetrics importer : structures) {
			Set<String> targets = new HashSet<>();
			for (String declaration : importer.imports()) {
				for (String token : importTokens(declaration)) {
					List<String> segments = List.of(token.split("/"));
					for (int index = 0; index < segments.size(); index++) {
						Set<String> matched = aliasTargets.get(String.join("/", segments.subList(index, segments.size())));
						if (matched != null) targets.addAll(matched);
					}
				}
			}
			targets.remove(importer.filePath());
			targets.forEach(target -> importersByTarget.computeIfAbsent(target, ignored -> new HashSet<>())
					.add(importer.filePath()));
		}
		Map<String, Integer> references = new HashMap<>();
		importersByTarget.forEach((path, importers) -> references.put(path, importers.size()));
		return references;
	}

	private List<String> importTokens(String declaration) {
		String normalized = declaration.toLowerCase(Locale.ROOT)
				.replace('\\', '/')
				.replace('.', '/')
				.replaceAll("[^a-z0-9_/$@-]+", " ");
		List<String> tokens = new ArrayList<>();
		for (String token : normalized.split("\\s+")) {
			String cleaned = token.replaceAll("^(?:import|from|using)/?", "")
					.replaceAll("^/+|/+$", "")
					.replaceAll("/(?:js|jsx|ts|tsx|java|py|cs)$", "");
			if (!cleaned.isBlank()) tokens.add(cleaned);
		}
		return tokens;
	}

	private String withoutExtension(String path) {
		int extension = path.lastIndexOf('.');
		return extension < 0 ? path : path.substring(0, extension);
	}

	private String basename(String path) {
		int separator = path.lastIndexOf('/');
		return separator < 0 ? path : path.substring(separator + 1);
	}

	private record RawMetric(
			String filePath,
			String language,
			long churn,
			int complexity,
			double contributorConcentration,
			int bugFixCommits,
			int dependencyReferences,
			int recentModifications) {
	}
}