package com.repoinsight.api.domain;

import java.util.UUID;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "file_metrics", uniqueConstraints = @UniqueConstraint(columnNames = { "analysis_id", "file_path", "period" }))
public class FileMetric {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "analysis_id", nullable = false)
	private RepositoryAnalysis analysis;

	@Column(name = "file_path", nullable = false, length = 2048)
	private String filePath;

	private String language;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private HistoryPeriod period;

	@Column(name = "commit_count", nullable = false)
	private int commitCount;

	@Column(nullable = false)
	private int additions;

	@Column(nullable = false)
	private int deletions;

	@Column(name = "contributor_count", nullable = false)
	private int contributorCount;

	@Column(name = "last_modified_at")
	private Instant lastModifiedAt;

	@Column(name = "total_churn", nullable = false)
	private long totalChurn;

	private Double complexity;

	@Column(name = "risk_score")
	private Double riskScore;

	protected FileMetric() {
	}

	public FileMetric(RepositoryAnalysis analysis, String filePath) {
		this.analysis = analysis;
		this.filePath = filePath;
	}

	public UUID getId() { return id; }
	public RepositoryAnalysis getAnalysis() { return analysis; }
	public String getFilePath() { return filePath; }
	public String getLanguage() { return language; }
	public HistoryPeriod getPeriod() { return period; }
	public int getCommitCount() { return commitCount; }
	public int getAdditions() { return additions; }
	public int getDeletions() { return deletions; }
	public int getContributorCount() { return contributorCount; }
	public Instant getLastModifiedAt() { return lastModifiedAt; }
	public long getTotalChurn() { return totalChurn; }
	public Double getComplexity() { return complexity; }
	public Double getRiskScore() { return riskScore; }
}