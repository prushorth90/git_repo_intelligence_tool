package com.repoinsight.api.analysis;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class AnalysisJobPublisher {

	private final StringRedisTemplate redisTemplate;
	private final String queueName;

	public AnalysisJobPublisher(
			StringRedisTemplate redisTemplate,
			@Value("${app.analysis.queue-name}") String queueName) {
		this.redisTemplate = redisTemplate;
		this.queueName = queueName;
	}

	public void publish(UUID repositoryId) {
		redisTemplate.opsForList().rightPush(queueName, repositoryId.toString());
	}
}