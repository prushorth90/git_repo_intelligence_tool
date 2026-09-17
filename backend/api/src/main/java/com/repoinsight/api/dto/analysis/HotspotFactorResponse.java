package com.repoinsight.api.dto.analysis;

public record HotspotFactorResponse(
		String key,
		String label,
		double rawValue,
		double normalizedValue,
		double weight,
		double contribution,
		String explanation) {
}