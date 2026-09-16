package com.repoinsight.worker.analysis;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class AnalysisJobProcessor {

	private static final Logger logger = LoggerFactory.getLogger(AnalysisJobProcessor.class);

	private final JdbcTemplate jdbcTemplate;
	private final AnalysisWorkload workload;
	private final AnalysisJobRetryQueue retryQueue;
	private final String workerId;
	private final int maxRetries;

	public AnalysisJobProcessor(
			JdbcTemplate jdbcTemplate,
			AnalysisWorkload workload,
			AnalysisJobRetryQueue retryQueue,
			@Value("${app.analysis.worker-id}") String workerId,
			@Value("${app.analysis.max-retries}") int maxRetries) {
		this.jdbcTemplate = jdbcTemplate;
		this.workload = workload;
		this.retryQueue = retryQueue;
		this.workerId = workerId;
		this.maxRetries = maxRetries;
	}

	public void process(UUID jobId) {
		if (!claim(jobId)) {
			logger.debug("Analysis job {} was already claimed or cancelled", jobId);
			return;
		}

		try {
			workload.analyze(jobId, this::updateProgress, this::isCancelled);
			complete(jobId);
		} catch (AnalysisCancelledException exception) {
			logger.info("Analysis job {} was cancelled", jobId);
		} catch (RuntimeException exception) {
			failOrRetry(jobId, exception);
		}
	}

	private boolean claim(UUID jobId) {
		return jdbcTemplate.update("""
				UPDATE analysis_jobs
				SET status = 'RUNNING', started_at = CURRENT_TIMESTAMP, completed_at = NULL,
				    progress_percentage = 1, worker_id = ?, heartbeat_at = CURRENT_TIMESTAMP, failure_reason = NULL
				WHERE id = ? AND status = 'QUEUED'
				""", workerId, jobId) == 1;
	}

	private void updateProgress(UUID jobId, int progress) {
		int updated = jdbcTemplate.update("""
				UPDATE analysis_jobs SET progress_percentage = ?, heartbeat_at = CURRENT_TIMESTAMP
				WHERE id = ? AND status = 'RUNNING' AND worker_id = ?
				""", progress, jobId, workerId);
		if (updated == 0) throw new AnalysisCancelledException();
	}

	private boolean isCancelled(UUID jobId) {
		Boolean cancelled = jdbcTemplate.queryForObject(
				"SELECT status = 'CANCELLED' FROM analysis_jobs WHERE id = ?", Boolean.class, jobId);
		return Boolean.TRUE.equals(cancelled);
	}

	private void complete(UUID jobId) {
		jdbcTemplate.update("""
				UPDATE analysis_jobs
				SET status = 'COMPLETED', completed_at = CURRENT_TIMESTAMP,
				    progress_percentage = 100, failure_reason = NULL, heartbeat_at = CURRENT_TIMESTAMP
				WHERE id = ? AND status = 'RUNNING' AND worker_id = ?
				""", jobId, workerId);
	}

	private void failOrRetry(UUID jobId, RuntimeException exception) {
		String reason = exception.getMessage() == null ? exception.getClass().getSimpleName() : exception.getMessage();
		Integer retries = jdbcTemplate.queryForObject(
				"SELECT retry_count FROM analysis_jobs WHERE id = ?", Integer.class, jobId);
		int nextRetry = (retries == null ? 0 : retries) + 1;
		if (nextRetry <= maxRetries) {
			int updated = jdbcTemplate.update("""
					UPDATE analysis_jobs
					SET status = 'QUEUED', started_at = NULL, progress_percentage = 0,
					    retry_count = ?, failure_reason = ?, worker_id = NULL, heartbeat_at = NULL
					WHERE id = ? AND status = 'RUNNING' AND worker_id = ?
					""", nextRetry, reason, jobId, workerId);
			if (updated == 1) retryQueue.enqueue(jobId);
		} else {
			jdbcTemplate.update("""
					UPDATE analysis_jobs
					SET status = 'FAILED', completed_at = CURRENT_TIMESTAMP,
					    retry_count = ?, failure_reason = ?
					WHERE id = ? AND status = 'RUNNING' AND worker_id = ?
					""", nextRetry, reason, jobId, workerId);
		}
		logger.error("Analysis job {} failed on attempt {}", jobId, nextRetry, exception);
	}
}