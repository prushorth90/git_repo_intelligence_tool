package com.repoinsight.api.dto.analysis;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.repoinsight.api.domain.HistoryPeriod;

public record CodeChurnRankingResponse(
		HistoryPeriod period,
		UUID analysisId,
		Instant analyzedAt,
		List<FileChurnResponse> files) {

	public static CodeChurnRankingResponse empty(HistoryPeriod period) {
		return new CodeChurnRankingResponse(period, null, null, List.of());
	}
}