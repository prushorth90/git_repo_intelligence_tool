package com.repoinsight.worker.analysis;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AnalysisJobRecovery {

	private final JdbcTemplate jdbcTemplate;
	private final StringRedisTemplate redisTemplate;
	private final AnalysisJobRetryQueue retryQueue;
	private final String processingQueueName;
	private final long staleAfterSeconds;
	private final int maxRetries;

	public AnalysisJobRecovery(
			JdbcTemplate jdbcTemplate,
			StringRedisTemplate redisTemplate,
			AnalysisJobRetryQueue retryQueue,
			@Value("${app.analysis.processing-queue-name}") String processingQueueName,
			@Value("${app.analysis.stale-after-seconds}") long staleAfterSeconds,
			@Value("${app.analysis.max-retries}") int maxRetries) {
		this.jdbcTemplate = jdbcTemplate;
		this.redisTemplate = redisTemplate;
		this.retryQueue = retryQueue;
		this.processingQueueName = processingQueueName;
		this.staleAfterSeconds = staleAfterSeconds;
		this.maxRetries = maxRetries;
	}

	@Scheduled(initialDelayString = "${app.analysis.recovery-delay-ms}", fixedDelayString = "${app.analysis.recovery-delay-ms}")
	public void recoverStaleJobs() {
		Instant cutoff = Instant.now().minusSeconds(staleAfterSeconds);
		var staleJobs = jdbcTemplate.query(
				"SELECT id, retry_count FROM analysis_jobs WHERE status = 'RUNNING' AND heartbeat_at < ?",
				(row, index) -> new StaleJob(row.getObject("id", UUID.class), row.getInt("retry_count")),
				Timestamp.from(cutoff));
		for (StaleJob staleJob : staleJobs) {
			UUID jobId = staleJob.id();
			int nextRetry = staleJob.retryCount() + 1;
			if (nextRetry > maxRetries) {
				int failed = jdbcTemplate.update("""
						UPDATE analysis_jobs
						SET status = 'FAILED', completed_at = CURRENT_TIMESTAMP, retry_count = ?,
						    failure_reason = 'Worker lease expired', heartbeat_at = NULL
						WHERE id = ? AND status = 'RUNNING' AND heartbeat_at < ?
						""", nextRetry, jobId, Timestamp.from(cutoff));
				if (failed == 1) redisTemplate.opsForList().remove(processingQueueName, 0, jobId.toString());
				continue;
			}
			int reset = jdbcTemplate.update("""
					UPDATE analysis_jobs
					SET status = 'QUEUED', started_at = NULL, progress_percentage = 0,
					    retry_count = ?, failure_reason = 'Worker lease expired',
					    worker_id = NULL, heartbeat_at = NULL
					WHERE id = ? AND status = 'RUNNING' AND heartbeat_at < ?
					""", nextRetry, jobId, Timestamp.from(cutoff));
			if (reset == 1) {
				redisTemplate.opsForList().remove(processingQueueName, 0, jobId.toString());
				retryQueue.enqueue(jobId);
			}
		}
	}

	private record StaleJob(UUID id, int retryCount) {
	}
}