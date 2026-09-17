package com.repoinsight.api.service;

import java.util.UUID;

import com.repoinsight.api.dto.analysis.FileHotspotResponse;
import com.repoinsight.api.dto.analysis.HotspotOverviewResponse;
import com.repoinsight.api.repository.FileHotspotMetricRepository;
import com.repoinsight.api.repository.RepositoryAnalysisRepository;
import com.repoinsight.api.repository.RepositoryRepository;
import com.repoinsight.api.service.exception.RepositoryNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DefaultHotspotService implements HotspotService {

	private final RepositoryRepository repositoryRepository;
	private final RepositoryAnalysisRepository analysisRepository;
	private final FileHotspotMetricRepository hotspotRepository;

	public DefaultHotspotService(
			RepositoryRepository repositoryRepository,
			RepositoryAnalysisRepository analysisRepository,
			FileHotspotMetricRepository hotspotRepository) {
		this.repositoryRepository = repositoryRepository;
		this.analysisRepository = analysisRepository;
		this.hotspotRepository = hotspotRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public HotspotOverviewResponse overview(UUID repositoryId) {
		if (!repositoryRepository.existsById(repositoryId)) throw new RepositoryNotFoundException(repositoryId);
		return analysisRepository.findFirstByRepositoryIdAndHistoryIncludedTrueOrderByAnalyzedAtDesc(repositoryId)
				.map(analysis -> new HotspotOverviewResponse(
						analysis.getId(), analysis.getAnalyzedAt(), HotspotOverviewResponse.DefaultScoringRules.FORMULA,
						HotspotOverviewResponse.DefaultScoringRules.NORMALIZATION,
						HotspotOverviewResponse.DefaultScoringRules.THRESHOLDS,
						hotspotRepository.findTop100ByAnalysisIdOrderByRiskScoreDescFilePathAsc(analysis.getId())
								.stream().map(FileHotspotResponse::from).toList()))
				.orElseGet(HotspotOverviewResponse::empty);
	}
}