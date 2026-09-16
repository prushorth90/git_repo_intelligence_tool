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
@Table(name = "repository_analyses")
public class RepositoryAnalysis {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "repository_id", nullable = false)
	private Repository repository;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private AnalysisStatus status;

	@Column(name = "health_score")
	private Double healthScore;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "analyzed_at")
	private Instant analyzedAt;

	protected RepositoryAnalysis() {
	}

	public RepositoryAnalysis(Repository repository) {
		this.repository = repository;
		this.status = AnalysisStatus.PENDING;
		this.createdAt = Instant.now();
	}

	public UUID getId() { return id; }
	public Repository getRepository() { return repository; }
	public AnalysisStatus getStatus() { return status; }
	public Double getHealthScore() { return healthScore; }
	public Instant getCreatedAt() { return createdAt; }
	public Instant getAnalyzedAt() { return analyzedAt; }
}