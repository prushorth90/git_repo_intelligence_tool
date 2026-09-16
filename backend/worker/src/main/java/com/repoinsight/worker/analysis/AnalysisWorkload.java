package com.repoinsight.worker.analysis;

import java.util.UUID;
import java.util.ArrayList;
import java.util.List;
import java.sql.Timestamp;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class AnalysisWorkload {

	private final JdbcTemplate jdbcTemplate;
	private final SecureRepositoryCloner repositoryCloner;
	private final SourceTreeAnalyzer sourceTreeAnalyzer;
	private final StructuralSourceAnalyzer structuralSourceAnalyzer;
	private final GitHistoryAnalyzer gitHistoryAnalyzer;
	private final WorkerAccessTokenCipher tokenCipher;
	private final ObjectMapper objectMapper;

	public AnalysisWorkload(
			JdbcTemplate jdbcTemplate,
			SecureRepositoryCloner repositoryCloner,
			SourceTreeAnalyzer sourceTreeAnalyzer,
			StructuralSourceAnalyzer structuralSourceAnalyzer,
			GitHistoryAnalyzer gitHistoryAnalyzer,
			WorkerAccessTokenCipher tokenCipher,
			ObjectMapper objectMapper) {
		this.jdbcTemplate = jdbcTemplate;
		this.repositoryCloner = repositoryCloner;
		this.sourceTreeAnalyzer = sourceTreeAnalyzer;
		this.structuralSourceAnalyzer = structuralSourceAnalyzer;
		this.gitHistoryAnalyzer = gitHistoryAnalyzer;
		this.tokenCipher = tokenCipher;
		this.objectMapper = objectMapper;
	}

	public void analyze(UUID jobId, BiConsumer<UUID, Integer> progress, Predicate<UUID> cancelled) {
		JobContext context = loadContext(jobId);
		checkCancelled(jobId, cancelled);
		progress.accept(jobId, 10);

		String accessToken = tokenCipher.decrypt(context.encryptedAccessToken());
		try (RepositoryCheckout checkout = repositoryCloner.cloneRepository(
				context.repositoryUrl(), context.defaultBranch(), accessToken, context.includeHistory())) {
			checkCancelled(jobId, cancelled);
			progress.accept(jobId, 40);
			SourceAnalysisResult result = sourceTreeAnalyzer.analyze(checkout.directory());
			checkCancelled(jobId, cancelled);
			progress.accept(jobId, 50);
			List<StructuralFileMetrics> structures = structuralSourceAnalyzer.analyze(
					checkout.directory(), jobId, progress, cancelled);
			checkCancelled(jobId, cancelled);
			progress.accept(jobId, 65);
			GitHistoryAnalysisResult history = context.includeHistory()
					? gitHistoryAnalyzer.analyze(checkout.directory(), jobId, cancelled)
					: GitHistoryAnalysisResult.empty();
			checkCancelled(jobId, cancelled);
			progress.accept(jobId, 88);
			UUID analysisId = persistResult(jobId, context, result);
			persistStructuralMetrics(analysisId, structures);
			persistHistoryMetrics(analysisId, history);
			progress.accept(jobId, 95);
		}
	}

	private void persistStructuralMetrics(UUID analysisId, List<StructuralFileMetrics> structures) {
		try {
			jdbcTemplate.update("DELETE FROM source_structure_metrics WHERE analysis_id = ?", analysisId);
			List<Object[]> rows = new ArrayList<>();
			for (StructuralFileMetrics metric : structures) {
				rows.add(new Object[] {
						UUID.randomUUID(), analysisId, metric.filePath(), metric.language(), metric.classCount(),
						metric.interfaceCount(), metric.methodCount(), metric.functionCount(), metric.imports().size(),
						metric.averageMethodLength(), metric.maximumMethodLength(), metric.maximumNestingDepth(),
						metric.controlFlowCount(), metric.parseError(), objectMapper.writeValueAsString(metric.symbols()),
						objectMapper.writeValueAsString(metric.imports())
				});
			}
			jdbcTemplate.batchUpdate("""
					INSERT INTO source_structure_metrics (
					    id, analysis_id, file_path, language, class_count, interface_count,
					    method_count, function_count, import_count, average_method_length,
					    maximum_method_length, maximum_nesting_depth, control_flow_count,
					    parse_error, symbols, imports)
					VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
					""", rows);
		} catch (JacksonException exception) {
			throw new IllegalStateException("Structural metadata serialization failed.", exception);
		}
	}

	private JobContext loadContext(UUID jobId) {
		return jdbcTemplate.queryForObject("""
				SELECT r.id AS repository_id, r.github_url, r.default_branch, j.include_history,
				       gc.encrypted_access_token
				FROM analysis_jobs j
				JOIN connected_repositories r ON r.id = j.repository_id
				LEFT JOIN github_connections gc ON gc.id = r.github_connection_id
				WHERE j.id = ?
				""", (row, index) -> new JobContext(
				row.getObject("repository_id", UUID.class),
				row.getString("github_url"),
				row.getString("default_branch"),
				row.getBoolean("include_history"),
				row.getString("encrypted_access_token")), jobId);
	}

	private UUID persistResult(UUID jobId, JobContext context, SourceAnalysisResult result) {
		try {
			UUID proposedAnalysisId = UUID.randomUUID();
			jdbcTemplate.update("""
					INSERT INTO repository_analyses (
					    id, repository_id, analysis_job_id, status, created_at, analyzed_at,
					    source_file_count, repository_size_bytes, language_distribution,
					    directory_structure, file_extensions, history_included)
					VALUES (?, ?, ?, 'COMPLETED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, ?, ?, ?, ?, ?, ?)
					ON CONFLICT (analysis_job_id) DO UPDATE SET
					    status = 'COMPLETED', analyzed_at = CURRENT_TIMESTAMP,
					    source_file_count = EXCLUDED.source_file_count,
					    repository_size_bytes = EXCLUDED.repository_size_bytes,
					    language_distribution = EXCLUDED.language_distribution,
					    directory_structure = EXCLUDED.directory_structure,
					    file_extensions = EXCLUDED.file_extensions,
					    history_included = EXCLUDED.history_included
					""",
					proposedAnalysisId, context.repositoryId(), jobId, result.sourceFileCount(),
					result.repositorySizeBytes(), objectMapper.writeValueAsString(result.languageDistribution()),
					objectMapper.writeValueAsString(result.directoryStructure()),
					objectMapper.writeValueAsString(result.fileExtensions()), context.includeHistory());
			return jdbcTemplate.queryForObject(
					"SELECT id FROM repository_analyses WHERE analysis_job_id = ?", UUID.class, jobId);
		} catch (JacksonException exception) {
			throw new IllegalStateException("Repository analysis result serialization failed.", exception);
		}
	}

	private void persistHistoryMetrics(UUID analysisId, GitHistoryAnalysisResult history) {
		if (history.metricsByPeriod().isEmpty()) return;
		try {
			jdbcTemplate.update("DELETE FROM file_ownership_metrics WHERE analysis_id = ?", analysisId);
			jdbcTemplate.update("DELETE FROM contributor_metrics WHERE analysis_id = ?", analysisId);
			jdbcTemplate.update("DELETE FROM file_metrics WHERE analysis_id = ?", analysisId);
			List<Object[]> fileRows = new ArrayList<>();
			history.metricsByPeriod().forEach((period, metrics) -> metrics.forEach(metric -> fileRows.add(new Object[] {
					UUID.randomUUID(), analysisId, metric.filePath(), metric.language(), period.name(), metric.commitCount(),
					metric.additions(), metric.deletions(), metric.uniqueContributors(), Timestamp.from(metric.lastModifiedAt()),
					metric.totalChurn(), metric.topContributorName(), metric.topOwnershipPercent(), metric.busFactor(),
					metric.concentratedOwnership()
			})));
			jdbcTemplate.batchUpdate("""
					INSERT INTO file_metrics (
					    id, analysis_id, file_path, language, period, commit_count, additions, deletions,
					    contributor_count, last_modified_at, total_churn, top_contributor_name,
					    top_ownership_percent, bus_factor, concentrated_ownership)
					VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
					""", fileRows);

			List<Object[]> ownershipRows = history.fileOwnership().stream().map(metric -> new Object[] {
					UUID.randomUUID(), analysisId, metric.filePath(), metric.contributorKey(), metric.displayName(),
					metric.commitCount(), metric.additions(), metric.deletions(), Timestamp.from(metric.lastModifiedAt()),
					metric.ownershipPercent()
			}).toList();
			jdbcTemplate.batchUpdate("""
					INSERT INTO file_ownership_metrics (
					    id, analysis_id, file_path, contributor_key, display_name, commit_count,
					    additions, deletions, last_modified_at, ownership_percent)
					VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
					""", ownershipRows);

			List<Object[]> contributorRows = new ArrayList<>();
			for (ContributorOwnershipResult contributor : history.contributors()) {
				contributorRows.add(new Object[] {
						UUID.randomUUID(), analysisId, contributor.contributorKey(), contributor.displayName(),
						contributor.totalCommits(), contributor.additions(), contributor.deletions(),
						contributor.estimatedOwnershipPercent(), contributor.filesTouched(),
						Timestamp.from(contributor.lastActivityAt()),
						objectMapper.writeValueAsString(contributor.primaryModules()), contributor.weightedScore()
				});
			}
			jdbcTemplate.batchUpdate("""
					INSERT INTO contributor_metrics (
					    id, analysis_id, contributor_key, display_name, commit_count, additions, deletions,
					    ownership_percent, files_touched, last_activity_at, primary_modules, weighted_score)
					VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
					""", contributorRows);
		} catch (JacksonException exception) {
			throw new IllegalStateException("Contributor module serialization failed.", exception);
		}
	}

	private void checkCancelled(UUID jobId, Predicate<UUID> cancelled) {
		if (cancelled.test(jobId)) throw new AnalysisCancelledException();
	}

	private record JobContext(
			UUID repositoryId,
			String repositoryUrl,
			String defaultBranch,
			boolean includeHistory,
			String encryptedAccessToken) {
	}
}