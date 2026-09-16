package com.repoinsight.api.repository;

import java.util.List;
import java.util.UUID;

import com.repoinsight.api.domain.SourceStructureMetric;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SourceStructureMetricRepository extends JpaRepository<SourceStructureMetric, UUID> {

	List<SourceStructureMetric> findTop100ByAnalysisIdAndParseErrorFalseOrderByCyclomaticComplexityDesc(UUID analysisId);
}