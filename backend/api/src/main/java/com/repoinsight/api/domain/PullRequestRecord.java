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
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "pull_request_records", uniqueConstraints = @UniqueConstraint(columnNames = { "repository_id", "github_number" }))
public class PullRequestRecord {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "repository_id", nullable = false)
	private Repository repository;

	@Column(name = "github_number", nullable = false)
	private int githubNumber;

	@Column(nullable = false)
	private String title;

	@Column(name = "author_login")
	private String authorLogin;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private PullRequestState state;

	@Column(name = "opened_at", nullable = false)
	private Instant openedAt;

	@Column(name = "closed_at")
	private Instant closedAt;

	@Column(name = "merged_at")
	private Instant mergedAt;

	protected PullRequestRecord() {
	}

	public PullRequestRecord(Repository repository, int githubNumber, String title, Instant openedAt) {
		this.repository = repository;
		this.githubNumber = githubNumber;
		this.title = title;
		this.openedAt = openedAt;
		this.state = PullRequestState.OPEN;
	}

	public UUID getId() { return id; }
	public Repository getRepository() { return repository; }
	public int getGithubNumber() { return githubNumber; }
	public String getTitle() { return title; }
	public String getAuthorLogin() { return authorLogin; }
	public PullRequestState getState() { return state; }
	public Instant getOpenedAt() { return openedAt; }
	public Instant getClosedAt() { return closedAt; }
	public Instant getMergedAt() { return mergedAt; }
}