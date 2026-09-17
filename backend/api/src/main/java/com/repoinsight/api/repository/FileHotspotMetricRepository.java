package com.repoinsight.api.repository;

import java.util.List;
import java.util.UUID;

import com.repoinsight.api.domain.FileHotspotMetric;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FileHotspotMetricRepository extends JpaRepository<FileHotspotMetric, UUID> {

	List<FileHotspotMetric> findTop100ByAnalysisIdOrderByRiskScoreDescFilePathAsc(UUID analysisId);
}