package com.repoinsight.api.repository;

import java.util.List;
import java.util.UUID;

import com.repoinsight.api.domain.ContributorMetric;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContributorMetricRepository extends JpaRepository<ContributorMetric, UUID> {
	List<ContributorMetric> findByAnalysisId(UUID analysisId);
}