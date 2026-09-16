package com.repoinsight.api.controller;

import java.util.UUID;

import com.repoinsight.api.dto.analysis.ContributorOverviewResponse;
import com.repoinsight.api.service.ContributorOwnershipService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/repositories/{repositoryId}/contributors")
public class ContributorOwnershipController {

	private final ContributorOwnershipService ownershipService;

	public ContributorOwnershipController(ContributorOwnershipService ownershipService) {
		this.ownershipService = ownershipService;
	}

	@GetMapping
	public ContributorOverviewResponse overview(@PathVariable UUID repositoryId) {
		return ownershipService.overview(repositoryId);
	}
}