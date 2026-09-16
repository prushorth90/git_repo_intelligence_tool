package com.repoinsight.worker.analysis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

class AnalysisJobProcessorTests {

	private final JdbcTemplate jdbcTemplate = new JdbcTemplate(new DriverManagerDataSource(
			"jdbc:h2:mem:processor;MODE=PostgreSQL;DB_CLOSE_DELAY=-1", "sa", ""));
	private final AnalysisWorkload workload = mock(AnalysisWorkload.class);
	private final AnalysisJobRetryQueue retryQueue = mock(AnalysisJobRetryQueue.class);

	@BeforeEach
	void setUp() {
		jdbcTemplate.execute("DROP TABLE IF EXISTS analysis_jobs");
		jdbcTemplate.execute("""
				CREATE TABLE analysis_jobs (
				    id UUID PRIMARY KEY,
				    status VARCHAR(32) NOT NULL,
				    requested_at TIMESTAMP WITH TIME ZONE NOT NULL,
				    started_at TIMESTAMP WITH TIME ZONE,
				    completed_at TIMESTAMP WITH TIME ZONE,
				    retry_count INTEGER NOT NULL,
				    progress_percentage INTEGER NOT NULL,
				    failure_reason VARCHAR(2048),
				    worker_id VARCHAR(255),
				    heartbeat_at TIMESTAMP WITH TIME ZONE
				)
				""");
	}

	@Test
	void requeuesFailedJobWithinRetryBudget() {
		UUID jobId = insertQueuedJob();
		doThrow(new IllegalStateException("analysis failed"))
				.when(workload).analyze(eq(jobId), any(), any());
		AnalysisJobProcessor processor = new AnalysisJobProcessor(jdbcTemplate, workload, retryQueue, "worker-a", 3);

		processor.process(jobId);

		Map<String, Object> row = jdbcTemplate.queryForMap(
				"SELECT status, retry_count, progress_percentage, failure_reason FROM analysis_jobs WHERE id = ?", jobId);
		assertThat(row.get("STATUS")).isEqualTo("QUEUED");
		assertThat(row.get("RETRY_COUNT")).isEqualTo(1);
		assertThat(row.get("PROGRESS_PERCENTAGE")).isEqualTo(0);
		assertThat(row.get("FAILURE_REASON")).isEqualTo("analysis failed");
		verify(retryQueue).enqueue(jobId);
	}

	@Test
	void marksFailedJobAfterRetryBudget() {
		UUID jobId = insertQueuedJob();
		doThrow(new IllegalStateException("analysis failed"))
				.when(workload).analyze(eq(jobId), any(), any());
		AnalysisJobProcessor processor = new AnalysisJobProcessor(jdbcTemplate, workload, retryQueue, "worker-b", 0);

		processor.process(jobId);

		Map<String, Object> row = jdbcTemplate.queryForMap(
				"SELECT status, retry_count, failure_reason FROM analysis_jobs WHERE id = ?", jobId);
		assertThat(row.get("STATUS")).isEqualTo("FAILED");
		assertThat(row.get("RETRY_COUNT")).isEqualTo(1);
		assertThat(row.get("FAILURE_REASON")).isEqualTo("analysis failed");
	}

	private UUID insertQueuedJob() {
		UUID jobId = UUID.randomUUID();
		jdbcTemplate.update("""
				INSERT INTO analysis_jobs (id, status, requested_at, retry_count, progress_percentage)
				VALUES (?, 'QUEUED', CURRENT_TIMESTAMP, 0, 0)
				""", jobId);
		return jobId;
	}
}