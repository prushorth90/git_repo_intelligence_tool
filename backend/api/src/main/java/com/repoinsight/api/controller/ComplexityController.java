package com.repoinsight.api.controller;

import java.util.UUID;

import com.repoinsight.api.dto.analysis.ComplexityOverviewResponse;
import com.repoinsight.api.service.ComplexityService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/repositories/{repositoryId}/complexity")
public class ComplexityController {

	private final ComplexityService complexityService;

	public ComplexityController(ComplexityService complexityService) {
		this.complexityService = complexityService;
	}

	@GetMapping
	public ComplexityOverviewResponse overview(@PathVariable UUID repositoryId) {
		return complexityService.overview(repositoryId);
	}
}