package com.repoinsight.api.controller;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import com.repoinsight.api.dto.repository.CreateRepositoryRequest;
import com.repoinsight.api.dto.repository.RepositoryResponse;
import com.repoinsight.api.dto.repository.UpdateRepositoryRequest;
import com.repoinsight.api.service.RepositoryService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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

	@GetMapping("/{id}")
	public RepositoryResponse findById(@PathVariable UUID id) {
		return RepositoryResponse.from(repositoryService.findById(id));
	}

	@PostMapping
	public ResponseEntity<RepositoryResponse> create(@Valid @RequestBody CreateRepositoryRequest request) {
		RepositoryResponse response = RepositoryResponse.from(repositoryService.create(request.githubUrl()));
		return ResponseEntity.created(URI.create("/api/repositories/" + response.id())).body(response);
	}

	@PutMapping("/{id}")
	public RepositoryResponse update(
			@PathVariable UUID id,
			@Valid @RequestBody UpdateRepositoryRequest request) {
		return RepositoryResponse.from(repositoryService.update(id, request.githubUrl()));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable UUID id) {
		repositoryService.delete(id);
		return ResponseEntity.noContent().build();
	}
}