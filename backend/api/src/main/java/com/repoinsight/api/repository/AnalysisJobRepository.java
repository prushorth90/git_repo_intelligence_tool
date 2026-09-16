package com.repoinsight.api.repository;

import java.util.List;
import java.util.UUID;

import com.repoinsight.api.domain.AnalysisJob;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnalysisJobRepository extends JpaRepository<AnalysisJob, UUID> {
	List<AnalysisJob> findByRepositoryIdOrderByRequestedAtDesc(UUID repositoryId);

	boolean existsByRepositoryIdAndStatusIn(UUID repositoryId, List<com.repoinsight.api.domain.AnalysisJobStatus> statuses);
}