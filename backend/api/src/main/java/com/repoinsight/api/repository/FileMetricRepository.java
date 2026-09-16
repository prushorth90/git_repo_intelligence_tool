package com.repoinsight.api.repository;

import java.util.List;
import java.util.UUID;

import com.repoinsight.api.domain.FileMetric;
import com.repoinsight.api.domain.HistoryPeriod;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileMetricRepository extends JpaRepository<FileMetric, UUID> {
	List<FileMetric> findByAnalysisId(UUID analysisId);

	List<FileMetric> findTop100ByAnalysisIdAndPeriodOrderByTotalChurnDesc(UUID analysisId, HistoryPeriod period);
}