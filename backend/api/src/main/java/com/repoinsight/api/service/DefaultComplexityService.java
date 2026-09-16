package com.repoinsight.api.service;

import java.util.List;
import java.util.UUID;

import com.repoinsight.api.dto.analysis.ComplexityOverviewResponse;
import com.repoinsight.api.dto.analysis.FileComplexityResponse;
import com.repoinsight.api.dto.analysis.MethodComplexityResponse;
import com.repoinsight.api.repository.MethodComplexityMetricRepository;
import com.repoinsight.api.repository.RepositoryAnalysisRepository;
import com.repoinsight.api.repository.RepositoryRepository;
import com.repoinsight.api.repository.SourceStructureMetricRepository;
import com.repoinsight.api.service.exception.RepositoryNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DefaultComplexityService implements ComplexityService {

	private static final String FORMULA = "Cyclomatic complexity = 1 + decision points";
	private static final List<String> RULES = List.of(
			"Count each if statement and loop",
			"Count each non-default switch or match case",
			"Count each catch or except branch",
			"Count each ternary conditional",
			"Count each direct logical AND or OR operator",
			"Exclude nested callable bodies from the parent callable score",
			"File score uses all decision points in the file");

	private final RepositoryRepository repositoryRepository;
	private final RepositoryAnalysisRepository analysisRepository;
	private final SourceStructureMetricRepository fileRepository;
	private final MethodComplexityMetricRepository methodRepository;

	public DefaultComplexityService(
			RepositoryRepository repositoryRepository,
			RepositoryAnalysisRepository analysisRepository,
			SourceStructureMetricRepository fileRepository,
			MethodComplexityMetricRepository methodRepository) {
		this.repositoryRepository = repositoryRepository;
		this.analysisRepository = analysisRepository;
		this.fileRepository = fileRepository;
		this.methodRepository = methodRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public ComplexityOverviewResponse overview(UUID repositoryId) {
		if (!repositoryRepository.existsById(repositoryId)) throw new RepositoryNotFoundException(repositoryId);
		return analysisRepository.findFirstByRepositoryIdOrderByAnalyzedAtDesc(repositoryId)
				.map(analysis -> new ComplexityOverviewResponse(
						analysis.getId(), analysis.getAnalyzedAt(), FORMULA, RULES,
						fileRepository.findTop100ByAnalysisIdAndParseErrorFalseOrderByCyclomaticComplexityDesc(analysis.getId())
								.stream().map(FileComplexityResponse::from).toList(),
						methodRepository.findTop100ByAnalysisIdOrderByCyclomaticComplexityDesc(analysis.getId())
								.stream().map(MethodComplexityResponse::from).toList()))
				.orElseGet(() -> ComplexityOverviewResponse.empty(FORMULA, RULES));
	}
}