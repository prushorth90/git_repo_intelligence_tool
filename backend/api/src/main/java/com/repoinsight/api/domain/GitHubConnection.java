package com.repoinsight.api.domain;

import java.io.Serializable;
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

@Entity
@Table(name = "github_connections")
public class GitHubConnection implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(name = "github_user_id", nullable = false, unique = true)
	private Long githubUserId;

	@Column(name = "github_login", nullable = false)
	private String githubLogin;

	@Column(name = "avatar_url", nullable = false)
	private String avatarUrl;

	@Column(name = "encrypted_access_token", nullable = false)
	private String encryptedAccessToken;

	@Column(nullable = false)
	private String scopes;

	@Column(name = "connected_at", nullable = false)
	private Instant connectedAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	protected GitHubConnection() {
	}

	public GitHubConnection(
			User user,
			Long githubUserId,
			String githubLogin,
			String avatarUrl,
			String encryptedAccessToken,
			String scopes) {
		this.user = user;
		this.githubUserId = githubUserId;
		this.githubLogin = githubLogin;
		this.avatarUrl = avatarUrl;
		this.encryptedAccessToken = encryptedAccessToken;
		this.scopes = scopes;
		this.connectedAt = Instant.now();
		this.updatedAt = connectedAt;
	}

	public void updateCredentials(
			String githubLogin,
			String avatarUrl,
			String encryptedAccessToken,
			String scopes) {
		this.githubLogin = githubLogin;
		this.avatarUrl = avatarUrl;
		this.encryptedAccessToken = encryptedAccessToken;
		this.scopes = scopes;
		this.updatedAt = Instant.now();
	}

	public UUID getId() { return id; }
	public User getUser() { return user; }
	public Long getGithubUserId() { return githubUserId; }
	public String getGithubLogin() { return githubLogin; }
	public String getAvatarUrl() { return avatarUrl; }
	public String getEncryptedAccessToken() { return encryptedAccessToken; }
	public String getScopes() { return scopes; }
	public Instant getConnectedAt() { return connectedAt; }
	public Instant getUpdatedAt() { return updatedAt; }
}