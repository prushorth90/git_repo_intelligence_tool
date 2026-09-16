package com.repoinsight.api.domain;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "analysis_jobs")
public class AnalysisJob {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "repository_id", nullable = false)
	private Repository repository;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private AnalysisJobStatus status;

	@Column(name = "requested_at", nullable = false)
	private Instant requestedAt;

	@Column(name = "started_at")
	private Instant startedAt;

	@Column(name = "completed_at")
	private Instant completedAt;

	@Column(name = "failure_reason", columnDefinition = "TEXT")
	private String failureReason;

	@Column(name = "retry_count", nullable = false)
	private int retryCount;

	@Column(name = "progress_percentage", nullable = false)
	private int progressPercentage;

	@Column(name = "worker_id")
	private String workerId;

	@Column(name = "heartbeat_at")
	private Instant heartbeatAt;

	protected AnalysisJob() {
	}

	public AnalysisJob(Repository repository) {
		this.repository = repository;
		this.status = AnalysisJobStatus.QUEUED;
		this.progressPercentage = 0;
		this.requestedAt = Instant.now();
	}

	public boolean cancel() {
		if (status != AnalysisJobStatus.QUEUED && status != AnalysisJobStatus.RUNNING) return false;
		status = AnalysisJobStatus.CANCELLED;
		completedAt = Instant.now();
		failureReason = null;
		return true;
	}

	public UUID getId() { return id; }
	public Repository getRepository() { return repository; }
	public AnalysisJobStatus getStatus() { return status; }
	public Instant getRequestedAt() { return requestedAt; }
	public Instant getStartedAt() { return startedAt; }
	public Instant getCompletedAt() { return completedAt; }
	public String getFailureReason() { return failureReason; }
	public int getRetryCount() { return retryCount; }
	public int getProgressPercentage() { return progressPercentage; }
	public String getWorkerId() { return workerId; }
	public Instant getHeartbeatAt() { return heartbeatAt; }
}