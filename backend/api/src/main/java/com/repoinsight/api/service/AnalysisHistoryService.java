package com.repoinsight.api.service;

import java.util.List;
import java.util.UUID;

import com.repoinsight.api.domain.AnalysisJob;

public interface AnalysisHistoryService {

	List<AnalysisJob> findByRepositoryId(UUID repositoryId);
}