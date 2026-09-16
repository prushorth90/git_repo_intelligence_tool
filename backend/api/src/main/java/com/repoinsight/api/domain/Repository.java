package com.repoinsight.api.domain;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "connected_repositories")
public class Repository implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(nullable = false)
	private String owner;

	@Column(nullable = false)
	private String name;

	@Column(name = "full_name", nullable = false, unique = true)
	private String fullName;

	@Column(name = "github_url", nullable = false)
	private String githubUrl;

	@Column(name = "github_repository_id")
	private Long githubRepositoryId;

	@Column(name = "default_branch", nullable = false)
	private String defaultBranch;

	@Column(name = "is_private", nullable = false)
	private boolean privateRepository;

	@Column(nullable = false)
	private String visibility;

	@Column(name = "primary_language")
	private String primaryLanguage;

	@Column(name = "stargazers_count", nullable = false)
	private int stars;

	@Column(name = "forks_count", nullable = false)
	private int forks;

	@Column(name = "github_updated_at")
	private Instant githubUpdatedAt;

	@ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
	@JoinColumn(name = "github_connection_id")
	private GitHubConnection githubConnection;

	@Column(name = "connected_at", nullable = false)
	private Instant connectedAt;

	protected Repository() {
	}

	public Repository(String owner, String name, String githubUrl) {
		this.owner = owner;
		this.name = name;
		this.fullName = owner + "/" + name;
		this.githubUrl = githubUrl;
		this.defaultBranch = "main";
		this.visibility = "public";
		this.connectedAt = Instant.now();
	}

	public Repository(GitHubRepositoryMetadata metadata, GitHubConnection githubConnection) {
		this.owner = metadata.owner();
		this.name = metadata.name();
		this.fullName = metadata.owner() + "/" + metadata.name();
		this.githubUrl = metadata.githubUrl();
		this.githubRepositoryId = metadata.githubRepositoryId();
		this.defaultBranch = metadata.defaultBranch();
		this.privateRepository = metadata.privateRepository();
		this.visibility = metadata.visibility();
		this.primaryLanguage = metadata.primaryLanguage();
		this.stars = metadata.stars();
		this.forks = metadata.forks();
		this.githubUpdatedAt = metadata.githubUpdatedAt();
		this.githubConnection = githubConnection;
		this.connectedAt = Instant.now();
	}

	public void update(String owner, String name, String githubUrl) {
		this.owner = owner;
		this.name = name;
		this.fullName = owner + "/" + name;
		this.githubUrl = githubUrl;
	}

	public UUID getId() {
		return id;
	}

	public String getOwner() {
		return owner;
	}

	public String getName() {
		return name;
	}

	public String getFullName() {
		return fullName;
	}

	public String getGithubUrl() {
		return githubUrl;
	}

	public Instant getConnectedAt() {
		return connectedAt;
	}

	public Long getGithubRepositoryId() {
		return githubRepositoryId;
	}

	public String getDefaultBranch() {
		return defaultBranch;
	}

	public boolean isPrivateRepository() {
		return privateRepository;
	}

	public GitHubConnection getGithubConnection() {
		return githubConnection;
	}

	public String getVisibility() { return visibility; }
	public String getPrimaryLanguage() { return primaryLanguage; }
	public int getStars() { return stars; }
	public int getForks() { return forks; }
	public Instant getGithubUpdatedAt() { return githubUpdatedAt; }

	public record GitHubRepositoryMetadata(
			long githubRepositoryId,
			String owner,
			String name,
			String githubUrl,
			String defaultBranch,
			String visibility,
			boolean privateRepository,
			String primaryLanguage,
			int stars,
			int forks,
			Instant githubUpdatedAt) {
	}
}