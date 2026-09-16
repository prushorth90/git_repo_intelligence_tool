package com.repoinsight.api.service;

import java.util.UUID;

public interface AnalysisJobQueue {

	void enqueue(UUID analysisJobId);
}