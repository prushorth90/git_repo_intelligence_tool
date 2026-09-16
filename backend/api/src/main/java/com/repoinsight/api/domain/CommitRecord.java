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
@Table(name = "commit_records", uniqueConstraints = @UniqueConstraint(columnNames = { "repository_id", "sha" }))
public class CommitRecord {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "repository_id", nullable = false)
	private Repository repository;

	@Column(nullable = false, length = 64)
	private String sha;

	@Column(name = "author_login")
	private String authorLogin;

	@Column(name = "commit_message", columnDefinition = "TEXT")
	private String message;

	@Column(name = "authored_at", nullable = false)
	private Instant authoredAt;

	@Column(nullable = false)
	private int additions;

	@Column(nullable = false)
	private int deletions;

	@Column(name = "files_changed", nullable = false)
	private int filesChanged;

	protected CommitRecord() {
	}

	public CommitRecord(Repository repository, String sha, Instant authoredAt) {
		this.repository = repository;
		this.sha = sha;
		this.authoredAt = authoredAt;
	}

	public UUID getId() { return id; }
	public Repository getRepository() { return repository; }
	public String getSha() { return sha; }
	public String getAuthorLogin() { return authorLogin; }
	public String getMessage() { return message; }
	public Instant getAuthoredAt() { return authoredAt; }
	public int getAdditions() { return additions; }
	public int getDeletions() { return deletions; }
	public int getFilesChanged() { return filesChanged; }
}