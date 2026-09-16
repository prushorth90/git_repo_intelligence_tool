package com.repoinsight.api.service;

import java.util.List;
import java.util.UUID;

import com.repoinsight.api.domain.ContributorMetric;
import com.repoinsight.api.domain.HistoryPeriod;
import com.repoinsight.api.dto.analysis.ConcentratedFileResponse;
import com.repoinsight.api.dto.analysis.ContributorOverviewResponse;
import com.repoinsight.api.dto.analysis.ContributorOwnershipResponse;
import com.repoinsight.api.repository.ContributorMetricRepository;
import com.repoinsight.api.repository.FileMetricRepository;
import com.repoinsight.api.repository.RepositoryAnalysisRepository;
import com.repoinsight.api.repository.RepositoryRepository;
import com.repoinsight.api.service.exception.RepositoryNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Service
public class DefaultContributorOwnershipService implements ContributorOwnershipService {

	private static final TypeReference<List<String>> STRING_LIST = new TypeReference<>() { };

	private final RepositoryRepository repositoryRepository;
	private final RepositoryAnalysisRepository analysisRepository;
	private final ContributorMetricRepository contributorRepository;
	private final FileMetricRepository fileMetricRepository;
	private final ObjectMapper objectMapper;

	public DefaultContributorOwnershipService(
			RepositoryRepository repositoryRepository,
			RepositoryAnalysisRepository analysisRepository,
			ContributorMetricRepository contributorRepository,
			FileMetricRepository fileMetricRepository,
			ObjectMapper objectMapper) {
		this.repositoryRepository = repositoryRepository;
		this.analysisRepository = analysisRepository;
		this.contributorRepository = contributorRepository;
		this.fileMetricRepository = fileMetricRepository;
		this.objectMapper = objectMapper;
	}

	@Override
	@Transactional(readOnly = true)
	public ContributorOverviewResponse overview(UUID repositoryId) {
		if (!repositoryRepository.existsById(repositoryId)) throw new RepositoryNotFoundException(repositoryId);
		return analysisRepository.findFirstByRepositoryIdAndHistoryIncludedTrueOrderByAnalyzedAtDesc(repositoryId)
				.map(analysis -> {
					List<ContributorOwnershipResponse> contributors = contributorRepository
							.findByAnalysisIdOrderByOwnershipPercentDesc(analysis.getId()).stream()
							.map(this::toResponse)
							.toList();
					List<ConcentratedFileResponse> concentratedFiles = fileMetricRepository
							.findTop100ByAnalysisIdAndPeriodAndConcentratedOwnershipTrueOrderByTopOwnershipPercentDesc(
									analysis.getId(), HistoryPeriod.ALL).stream()
							.map(metric -> new ConcentratedFileResponse(
									metric.getFilePath(), metric.getLanguage(), metric.getTopContributorName(),
									metric.getTopOwnershipPercent(), metric.getBusFactor(), metric.getTotalChurn()))
							.toList();
					return new ContributorOverviewResponse(
							analysis.getId(), analysis.getAnalyzedAt(), repositoryBusFactor(contributors),
							concentratedFiles.size(), contributors, concentratedFiles);
				})
				.orElseGet(ContributorOverviewResponse::empty);
	}

	private ContributorOwnershipResponse toResponse(ContributorMetric metric) {
		try {
			return new ContributorOwnershipResponse(
					metric.getContributorKey(), metric.getDisplayName(), metric.getCommitCount(), metric.getFilesTouched(),
					metric.getAdditions(), metric.getDeletions(), metric.getOwnershipPercent(), metric.getLastActivityAt(),
					objectMapper.readValue(metric.getPrimaryModules(), STRING_LIST));
		} catch (tools.jackson.core.JacksonException exception) {
			throw new IllegalStateException("Contributor modules could not be read.", exception);
		}
	}

	private int repositoryBusFactor(List<ContributorOwnershipResponse> contributors) {
		double cumulative = 0;
		int factor = 0;
		for (ContributorOwnershipResponse contributor : contributors) {
			cumulative += contributor.estimatedOwnershipPercent();
			factor++;
			if (cumulative >= 50.0) break;
		}
		return factor;
	}
}