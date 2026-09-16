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
}