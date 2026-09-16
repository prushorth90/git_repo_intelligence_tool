package com.repoinsight.api.controller;

import java.util.UUID;

import com.repoinsight.api.domain.HistoryPeriod;
import com.repoinsight.api.dto.analysis.CodeChurnRankingResponse;
import com.repoinsight.api.service.CodeChurnService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/repositories/{repositoryId}/code-churn")
public class CodeChurnController {

	private final CodeChurnService codeChurnService;

	public CodeChurnController(CodeChurnService codeChurnService) {
		this.codeChurnService = codeChurnService;
	}

	@GetMapping
	public CodeChurnRankingResponse rankings(
			@PathVariable UUID repositoryId,
			@RequestParam(defaultValue = "DAYS_90") HistoryPeriod period) {
		return codeChurnService.rankings(repositoryId, period);
	}
}