package com.repoinsight.api.repository;

import java.util.List;
import java.util.UUID;

import com.repoinsight.api.domain.MethodComplexityMetric;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MethodComplexityMetricRepository extends JpaRepository<MethodComplexityMetric, UUID> {

	List<MethodComplexityMetric> findTop100ByAnalysisIdOrderByCyclomaticComplexityDesc(UUID analysisId);
}