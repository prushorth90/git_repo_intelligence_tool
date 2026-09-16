package com.repoinsight.api.repository;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/repositories")
public class RepositoryController {

	private final RepositoryService repositoryService;

	public RepositoryController(RepositoryService repositoryService) {
		this.repositoryService = repositoryService;
	}

	@GetMapping
	public List<RepositoryResponse> findAll() {
		return repositoryService.findAll().stream().map(RepositoryResponse::from).toList();
	}

	@PostMapping
	public ResponseEntity<RepositoryResponse> connect(@Valid @RequestBody ConnectRepositoryRequest request) {
		RepositoryResponse response = RepositoryResponse.from(repositoryService.connect(request.githubUrl()));
		return ResponseEntity.created(URI.create("/api/repositories/" + response.id())).body(response);
	}

	public record ConnectRepositoryRequest(@NotBlank String githubUrl) {
	}

	public record RepositoryResponse(
			UUID id, String owner, String name, String fullName, String githubUrl, Instant connectedAt) {

		static RepositoryResponse from(ConnectedRepository repository) {
			return new RepositoryResponse(repository.getId(), repository.getOwner(), repository.getName(),
					repository.getFullName(), repository.getGithubUrl(), repository.getConnectedAt());
		}
	}
}