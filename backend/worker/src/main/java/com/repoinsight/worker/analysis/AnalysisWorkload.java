package com.repoinsight.worker.analysis;

import java.util.UUID;
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
	private final WorkerAccessTokenCipher tokenCipher;
	private final ObjectMapper objectMapper;

	public AnalysisWorkload(
			JdbcTemplate jdbcTemplate,
			SecureRepositoryCloner repositoryCloner,
			SourceTreeAnalyzer sourceTreeAnalyzer,
			WorkerAccessTokenCipher tokenCipher,
			ObjectMapper objectMapper) {
		this.jdbcTemplate = jdbcTemplate;
		this.repositoryCloner = repositoryCloner;
		this.sourceTreeAnalyzer = sourceTreeAnalyzer;
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
			progress.accept(jobId, 85);
			persistResult(jobId, context, result);
			progress.accept(jobId, 95);
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

	private void persistResult(UUID jobId, JobContext context, SourceAnalysisResult result) {
		try {
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
					UUID.randomUUID(), context.repositoryId(), jobId, result.sourceFileCount(),
					result.repositorySizeBytes(), objectMapper.writeValueAsString(result.languageDistribution()),
					objectMapper.writeValueAsString(result.directoryStructure()),
					objectMapper.writeValueAsString(result.fileExtensions()), context.includeHistory());
		} catch (JacksonException exception) {
			throw new IllegalStateException("Repository analysis result serialization failed.", exception);
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