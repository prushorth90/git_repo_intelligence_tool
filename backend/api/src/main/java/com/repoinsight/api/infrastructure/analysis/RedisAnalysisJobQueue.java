package com.repoinsight.api.infrastructure.analysis;

import java.util.UUID;

import com.repoinsight.api.service.AnalysisJobQueue;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisAnalysisJobQueue implements AnalysisJobQueue {

	private final StringRedisTemplate redisTemplate;
	private final String queueName;

	public RedisAnalysisJobQueue(
			StringRedisTemplate redisTemplate,
			@Value("${app.analysis.queue-name}") String queueName) {
		this.redisTemplate = redisTemplate;
		this.queueName = queueName;
	}

	@Override
	public void enqueue(UUID analysisJobId) {
		redisTemplate.opsForList().rightPush(queueName, analysisJobId.toString());
	}
}