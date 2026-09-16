package com.repoinsight.api.service;

import java.util.List;
import java.util.UUID;

import com.repoinsight.api.domain.AnalysisJob;
import com.repoinsight.api.repository.AnalysisJobRepository;
import com.repoinsight.api.repository.RepositoryRepository;
import com.repoinsight.api.service.exception.RepositoryNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DefaultAnalysisHistoryService implements AnalysisHistoryService {

	private final RepositoryRepository repositoryRepository;
	private final AnalysisJobRepository analysisJobRepository;

	public DefaultAnalysisHistoryService(
			RepositoryRepository repositoryRepository,
			AnalysisJobRepository analysisJobRepository) {
		this.repositoryRepository = repositoryRepository;
		this.analysisJobRepository = analysisJobRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public List<AnalysisJob> findByRepositoryId(UUID repositoryId) {
		if (!repositoryRepository.existsById(repositoryId)) {
			throw new RepositoryNotFoundException(repositoryId);
		}
		return analysisJobRepository.findByRepositoryIdOrderByRequestedAtDesc(repositoryId);
	}
}