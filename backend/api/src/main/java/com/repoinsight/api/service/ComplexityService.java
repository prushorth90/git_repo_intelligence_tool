package com.repoinsight.api.service;

import java.util.UUID;

import com.repoinsight.api.dto.analysis.ComplexityOverviewResponse;

public interface ComplexityService {

	ComplexityOverviewResponse overview(UUID repositoryId);
}