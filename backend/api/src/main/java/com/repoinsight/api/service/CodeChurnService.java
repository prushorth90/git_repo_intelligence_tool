package com.repoinsight.api.service;

import java.util.UUID;

import com.repoinsight.api.domain.HistoryPeriod;
import com.repoinsight.api.dto.analysis.CodeChurnRankingResponse;

public interface CodeChurnService {

	CodeChurnRankingResponse rankings(UUID repositoryId, HistoryPeriod period);
}