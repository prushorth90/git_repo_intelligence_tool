package com.repoinsight.api.domain;

import java.time.Instant;
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
@Table(name = "file_ownership_metrics", uniqueConstraints = @UniqueConstraint(
		columnNames = { "analysis_id", "file_path", "contributor_key" }))
public class FileOwnershipMetric {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "analysis_id", nullable = false)
	private RepositoryAnalysis analysis;

	@Column(name = "file_path", nullable = false, length = 2048)
	private String filePath;

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

	@Column(name = "last_modified_at")
	private Instant lastModifiedAt;

	@Column(name = "ownership_percent", nullable = false)
	private double ownershipPercent;

	protected FileOwnershipMetric() {
	}

	public UUID getId() { return id; }
	public String getFilePath() { return filePath; }
	public String getDisplayName() { return displayName; }
	public double getOwnershipPercent() { return ownershipPercent; }
}