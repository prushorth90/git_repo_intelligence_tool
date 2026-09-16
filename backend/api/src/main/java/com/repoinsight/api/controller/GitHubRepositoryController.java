package com.repoinsight.api.controller;

import java.net.URI;

import com.repoinsight.api.dto.github.GitHubRepositoryPageResponse;
import com.repoinsight.api.dto.repository.RepositoryResponse;
import com.repoinsight.api.service.GitHubRepositoryCatalogService;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/github/repositories")
public class GitHubRepositoryController {

	private final GitHubRepositoryCatalogService catalogService;

	public GitHubRepositoryController(GitHubRepositoryCatalogService catalogService) {
		this.catalogService = catalogService;
	}

	@GetMapping
	public GitHubRepositoryPageResponse findRepositories(
			OAuth2AuthenticationToken authentication,
			@RequestParam(defaultValue = "1") @Min(1) int page,
			@RequestParam(defaultValue = "30") @Min(1) @Max(100) int perPage) {
		return GitHubRepositoryPageResponse.from(
				catalogService.findRepositories(githubUserId(authentication), page, perPage));
	}

	@PostMapping("/{githubRepositoryId}/import")
	public ResponseEntity<RepositoryResponse> importRepository(
			OAuth2AuthenticationToken authentication,
			@PathVariable long githubRepositoryId) {
		RepositoryResponse response = RepositoryResponse.from(
				catalogService.importRepository(githubUserId(authentication), githubRepositoryId));
		return ResponseEntity.created(URI.create("/api/repositories/" + response.id())).body(response);
	}

	private long githubUserId(OAuth2AuthenticationToken authentication) {
		Object id = authentication.getPrincipal().getAttribute("id");
		if (id instanceof Number number) return number.longValue();
		throw new IllegalArgumentException("GitHub did not provide the required id attribute.");
	}
}