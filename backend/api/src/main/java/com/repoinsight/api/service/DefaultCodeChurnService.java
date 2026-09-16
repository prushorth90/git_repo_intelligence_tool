package com.repoinsight.api.service;

import java.util.UUID;

import com.repoinsight.api.domain.HistoryPeriod;
import com.repoinsight.api.dto.analysis.CodeChurnRankingResponse;
import com.repoinsight.api.dto.analysis.FileChurnResponse;
import com.repoinsight.api.repository.FileMetricRepository;
import com.repoinsight.api.repository.RepositoryAnalysisRepository;
import com.repoinsight.api.repository.RepositoryRepository;
import com.repoinsight.api.service.exception.RepositoryNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DefaultCodeChurnService implements CodeChurnService {

	private final RepositoryRepository repositoryRepository;
	private final RepositoryAnalysisRepository analysisRepository;
	private final FileMetricRepository fileMetricRepository;

	public DefaultCodeChurnService(
			RepositoryRepository repositoryRepository,
			RepositoryAnalysisRepository analysisRepository,
			FileMetricRepository fileMetricRepository) {
		this.repositoryRepository = repositoryRepository;
		this.analysisRepository = analysisRepository;
		this.fileMetricRepository = fileMetricRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public CodeChurnRankingResponse rankings(UUID repositoryId, HistoryPeriod period) {
		if (!repositoryRepository.existsById(repositoryId)) throw new RepositoryNotFoundException(repositoryId);
		return analysisRepository.findFirstByRepositoryIdAndHistoryIncludedTrueOrderByAnalyzedAtDesc(repositoryId)
				.map(analysis -> new CodeChurnRankingResponse(
						period,
						analysis.getId(),
						analysis.getAnalyzedAt(),
						fileMetricRepository.findTop100ByAnalysisIdAndPeriodOrderByTotalChurnDesc(analysis.getId(), period)
								.stream().map(FileChurnResponse::from).toList()))
				.orElseGet(() -> CodeChurnRankingResponse.empty(period));
	}
}