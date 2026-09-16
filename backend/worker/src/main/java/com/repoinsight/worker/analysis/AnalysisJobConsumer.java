package com.repoinsight.worker.analysis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AnalysisJobConsumer {

	private static final Logger logger = LoggerFactory.getLogger(AnalysisJobConsumer.class);

	private final StringRedisTemplate redisTemplate;
	private final String queueName;

	public AnalysisJobConsumer(
			StringRedisTemplate redisTemplate,
			@Value("${app.analysis.queue-name}") String queueName) {
		this.redisTemplate = redisTemplate;
		this.queueName = queueName;
	}

	@Scheduled(initialDelay = 10_000, fixedDelayString = "${app.analysis.poll-delay-ms}")
	public void poll() {
		String repositoryId = redisTemplate.opsForList().leftPop(queueName);
		if (repositoryId != null) {
			logger.info("Accepted placeholder analysis job for repository {}", repositoryId);
		}
	}
}