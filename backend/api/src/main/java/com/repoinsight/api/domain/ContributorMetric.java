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
@Table(name = "contributor_metrics", uniqueConstraints = @UniqueConstraint(columnNames = { "analysis_id", "github_login" }))
public class ContributorMetric {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "analysis_id", nullable = false)
	private RepositoryAnalysis analysis;

	@Column(name = "github_login", nullable = false)
	private String githubLogin;

	@Column(name = "commit_count", nullable = false)
	private int commitCount;

	@Column(nullable = false)
	private int additions;

	@Column(nullable = false)
	private int deletions;

	@Column(name = "ownership_percent")
	private Double ownershipPercent;

	protected ContributorMetric() {
	}

	public ContributorMetric(RepositoryAnalysis analysis, String githubLogin) {
		this.analysis = analysis;
		this.githubLogin = githubLogin;
	}

	public UUID getId() { return id; }
	public RepositoryAnalysis getAnalysis() { return analysis; }
	public String getGithubLogin() { return githubLogin; }
	public int getCommitCount() { return commitCount; }
	public int getAdditions() { return additions; }
	public int getDeletions() { return deletions; }
	public Double getOwnershipPercent() { return ownershipPercent; }
}