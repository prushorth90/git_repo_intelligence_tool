package com.repoinsight.api.domain;

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
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "file_hotspot_metrics", uniqueConstraints = @UniqueConstraint(columnNames = { "analysis_id", "file_path" }))
public class FileHotspotMetric {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "analysis_id", nullable = false)
	private RepositoryAnalysis analysis;

	@Column(name = "file_path", nullable = false, length = 2048)
	private String filePath;

	@Column(nullable = false)
	private String language;

	@Column(nullable = false)
	private long churn;

	@Column(nullable = false)
	private int complexity;

	@Column(name = "contributor_concentration", nullable = false)
	private double contributorConcentration;

	@Column(name = "bug_fix_commits", nullable = false)
	private int bugFixCommits;

	@Column(name = "dependency_references", nullable = false)
	private int dependencyReferences;

	@Column(name = "recent_modifications", nullable = false)
	private int recentModifications;

	@Column(name = "normalized_churn", nullable = false)
	private double normalizedChurn;

	@Column(name = "normalized_complexity", nullable = false)
	private double normalizedComplexity;

	@Column(name = "normalized_contributor_concentration", nullable = false)
	private double normalizedContributorConcentration;

	@Column(name = "normalized_bug_fix_activity", nullable = false)
	private double normalizedBugFixActivity;

	@Column(name = "normalized_dependency_importance", nullable = false)
	private double normalizedDependencyImportance;

	@Column(name = "normalized_modification_frequency", nullable = false)
	private double normalizedModificationFrequency;

	@Column(name = "risk_score", nullable = false)
	private double riskScore;

	@Enumerated(EnumType.STRING)
	@Column(name = "risk_level", nullable = false)
	private HotspotRiskLevel riskLevel;

	protected FileHotspotMetric() {
	}

	public UUID getId() { return id; }
	public String getFilePath() { return filePath; }
	public String getLanguage() { return language; }
	public long getChurn() { return churn; }
	public int getComplexity() { return complexity; }
	public double getContributorConcentration() { return contributorConcentration; }
	public int getBugFixCommits() { return bugFixCommits; }
	public int getDependencyReferences() { return dependencyReferences; }
	public int getRecentModifications() { return recentModifications; }
	public double getNormalizedChurn() { return normalizedChurn; }
	public double getNormalizedComplexity() { return normalizedComplexity; }
	public double getNormalizedContributorConcentration() { return normalizedContributorConcentration; }
	public double getNormalizedBugFixActivity() { return normalizedBugFixActivity; }
	public double getNormalizedDependencyImportance() { return normalizedDependencyImportance; }
	public double getNormalizedModificationFrequency() { return normalizedModificationFrequency; }
	public double getRiskScore() { return riskScore; }
	public HotspotRiskLevel getRiskLevel() { return riskLevel; }
}