package com.repoinsight.api.domain;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "contributor_metrics", uniqueConstraints = @UniqueConstraint(columnNames = { "analysis_id", "contributor_key" }))
public class ContributorMetric {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "analysis_id", nullable = false)
	private RepositoryAnalysis analysis;

	@Column(name = "contributor_key", nullable = false)
	private String contributorKey;

	@Column(name = "display_name", nullable = false)
	private String displayName;

	@Column(name = "commit_count", nullable = false)
	private int commitCount;

	@Column(nullable = false)
	private int additions;

	@Column(nullable = false)
	private int deletions;

	@Column(name = "ownership_percent")
	private Double ownershipPercent;

	@Column(name = "files_touched", nullable = false)
	private int filesTouched;

	@Column(name = "last_activity_at")
	private java.time.Instant lastActivityAt;

	@Column(name = "primary_modules", nullable = false, columnDefinition = "TEXT")
	private String primaryModules;

	@Column(name = "weighted_score", nullable = false)
	private double weightedScore;

	protected ContributorMetric() {
	}

	public ContributorMetric(RepositoryAnalysis analysis, String contributorKey) {
		this.analysis = analysis;
		this.contributorKey = contributorKey;
	}

	public UUID getId() { return id; }
	public RepositoryAnalysis getAnalysis() { return analysis; }
	public String getContributorKey() { return contributorKey; }
	public String getDisplayName() { return displayName; }
	public int getCommitCount() { return commitCount; }
	public int getAdditions() { return additions; }
	public int getDeletions() { return deletions; }
	public Double getOwnershipPercent() { return ownershipPercent; }
	public int getFilesTouched() { return filesTouched; }
	public java.time.Instant getLastActivityAt() { return lastActivityAt; }
	public String getPrimaryModules() { return primaryModules; }
	public double getWeightedScore() { return weightedScore; }
}