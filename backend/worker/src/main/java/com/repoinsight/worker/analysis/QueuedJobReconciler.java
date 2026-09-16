package com.repoinsight.worker.analysis;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class QueuedJobReconciler {

	private final JdbcTemplate jdbcTemplate;
	private final AnalysisJobRetryQueue retryQueue;

	public QueuedJobReconciler(JdbcTemplate jdbcTemplate, AnalysisJobRetryQueue retryQueue) {
		this.jdbcTemplate = jdbcTemplate;
		this.retryQueue = retryQueue;
	}

	@Scheduled(
			initialDelayString = "${app.analysis.reconciliation-delay-ms}",
			fixedDelayString = "${app.analysis.reconciliation-delay-ms}")
	public void republishQueuedJobs() {
		Instant cutoff = Instant.now().minusSeconds(30);
		jdbcTemplate.query(
				"SELECT id FROM analysis_jobs WHERE status = 'QUEUED' AND requested_at < ? ORDER BY requested_at LIMIT 100",
				(row, index) -> row.getObject("id", UUID.class), Timestamp.from(cutoff))
				.forEach(retryQueue::enqueue);
	}
}