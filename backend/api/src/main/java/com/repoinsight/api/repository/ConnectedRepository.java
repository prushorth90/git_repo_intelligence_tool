package com.repoinsight.api.repository;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "connected_repositories")
public class ConnectedRepository implements Serializable {

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

	@Column(name = "connected_at", nullable = false)
	private Instant connectedAt;

	protected ConnectedRepository() {
	}

	public ConnectedRepository(String owner, String name, String githubUrl) {
		this.owner = owner;
		this.name = name;
		this.fullName = owner + "/" + name;
		this.githubUrl = githubUrl;
		this.connectedAt = Instant.now();
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
}