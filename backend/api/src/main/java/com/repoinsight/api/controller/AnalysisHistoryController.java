package com.repoinsight.api.controller;

import java.util.List;
import java.util.UUID;

import com.repoinsight.api.dto.analysis.AnalysisJobResponse;
import com.repoinsight.api.service.AnalysisHistoryService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/repositories/{repositoryId}/analysis-jobs")
public class AnalysisHistoryController {

	private final AnalysisHistoryService analysisHistoryService;

	public AnalysisHistoryController(AnalysisHistoryService analysisHistoryService) {
		this.analysisHistoryService = analysisHistoryService;
	}

	@GetMapping
	public List<AnalysisJobResponse> findByRepositoryId(@PathVariable UUID repositoryId) {
		return analysisHistoryService.findByRepositoryId(repositoryId).stream()
				.map(AnalysisJobResponse::from)
				.toList();
	}
}