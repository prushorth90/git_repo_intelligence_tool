package com.repoinsight.api.service;

import java.util.List;
import java.util.UUID;

import com.repoinsight.api.domain.AnalysisJob;
import com.repoinsight.api.domain.AnalysisJobStatus;
import com.repoinsight.api.repository.AnalysisJobRepository;
import com.repoinsight.api.repository.RepositoryRepository;
import com.repoinsight.api.service.exception.RepositoryNotFoundException;
import com.repoinsight.api.service.exception.AnalysisJobAlreadyActiveException;
import com.repoinsight.api.service.exception.AnalysisJobNotCancellableException;
import com.repoinsight.api.service.exception.AnalysisJobNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DefaultAnalysisHistoryService implements AnalysisHistoryService {

	private final RepositoryRepository repositoryRepository;
	private final AnalysisJobRepository analysisJobRepository;
	private final AnalysisJobQueue analysisJobQueue;

	public DefaultAnalysisHistoryService(
			RepositoryRepository repositoryRepository,
			AnalysisJobRepository analysisJobRepository,
			AnalysisJobQueue analysisJobQueue) {
		this.repositoryRepository = repositoryRepository;
		this.analysisJobRepository = analysisJobRepository;
		this.analysisJobQueue = analysisJobQueue;
	}

	@Override
	@Transactional(readOnly = true)
	public List<AnalysisJob> findByRepositoryId(UUID repositoryId) {
		if (!repositoryRepository.existsById(repositoryId)) {
			throw new RepositoryNotFoundException(repositoryId);
		}
		return analysisJobRepository.findByRepositoryIdOrderByRequestedAtDesc(repositoryId);
	}

	@Override
	@Transactional
	public AnalysisJob requestAnalysis(UUID repositoryId, boolean includeHistory) {
		var repository = repositoryRepository.findById(repositoryId)
				.orElseThrow(() -> new RepositoryNotFoundException(repositoryId));
		if (analysisJobRepository.existsByRepositoryIdAndStatusIn(
				repositoryId, List.of(AnalysisJobStatus.QUEUED, AnalysisJobStatus.RUNNING))) {
			throw new AnalysisJobAlreadyActiveException();
		}
		AnalysisJob job = analysisJobRepository.save(new AnalysisJob(repository, includeHistory));
		analysisJobQueue.enqueue(job.getId());
		return job;
	}

	@Override
	@Transactional
	public AnalysisJob cancel(UUID repositoryId, UUID jobId) {
		AnalysisJob job = analysisJobRepository.findById(jobId)
				.filter(candidate -> candidate.getRepository().getId().equals(repositoryId))
				.orElseThrow(() -> new AnalysisJobNotFoundException(jobId));
		if (!job.cancel()) throw new AnalysisJobNotCancellableException();
		return job;
	}
}