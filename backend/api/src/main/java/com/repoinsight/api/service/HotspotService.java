package com.repoinsight.api.service;

import java.util.UUID;

import com.repoinsight.api.dto.analysis.HotspotOverviewResponse;

public interface HotspotService {

	HotspotOverviewResponse overview(UUID repositoryId);
}