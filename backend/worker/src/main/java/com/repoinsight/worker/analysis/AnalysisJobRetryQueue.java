package com.repoinsight.worker.analysis;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class AnalysisJobRetryQueue {

	private final StringRedisTemplate redisTemplate;
	private final String queueName;

	public AnalysisJobRetryQueue(
			StringRedisTemplate redisTemplate,
			@Value("${app.analysis.queue-name}") String queueName) {
		this.redisTemplate = redisTemplate;
		this.queueName = queueName;
	}

	public void enqueue(UUID jobId) {
		redisTemplate.opsForList().rightPush(queueName, jobId.toString());
	}
}