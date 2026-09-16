package com.repoinsight.api.service;

import java.util.UUID;

import com.repoinsight.api.dto.analysis.ContributorOverviewResponse;

public interface ContributorOwnershipService {

	ContributorOverviewResponse overview(UUID repositoryId);
}