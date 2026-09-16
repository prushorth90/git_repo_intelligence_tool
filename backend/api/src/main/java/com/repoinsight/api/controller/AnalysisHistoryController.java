package com.repoinsight.api.controller;

import java.util.List;
import java.util.UUID;

import com.repoinsight.api.dto.analysis.AnalysisJobResponse;
import com.repoinsight.api.service.AnalysisHistoryService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

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

	@PostMapping
	@ResponseStatus(HttpStatus.ACCEPTED)
	public AnalysisJobResponse requestAnalysis(@PathVariable UUID repositoryId) {
		return AnalysisJobResponse.from(analysisHistoryService.requestAnalysis(repositoryId));
	}

	@PostMapping("/{jobId}/cancel")
	public AnalysisJobResponse cancel(@PathVariable UUID repositoryId, @PathVariable UUID jobId) {
		return AnalysisJobResponse.from(analysisHistoryService.cancel(repositoryId, jobId));
	}
}