package com.repoinsight.api.repository;

import java.util.List;
import java.util.UUID;

import com.repoinsight.api.domain.RepositoryAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RepositoryAnalysisRepository extends JpaRepository<RepositoryAnalysis, UUID> {
	List<RepositoryAnalysis> findByRepositoryIdOrderByCreatedAtDesc(UUID repositoryId);

	java.util.Optional<RepositoryAnalysis> findFirstByRepositoryIdAndHistoryIncludedTrueOrderByAnalyzedAtDesc(UUID repositoryId);
}