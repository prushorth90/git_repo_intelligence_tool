package com.repoinsight.api.infrastructure.analysis;

import java.util.UUID;

import com.repoinsight.api.service.AnalysisJobQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
public class RedisAnalysisJobQueue implements AnalysisJobQueue {

	private static final Logger logger = LoggerFactory.getLogger(RedisAnalysisJobQueue.class);

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
		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
				@Override
				public void afterCommit() {
					try {
						publish(analysisJobId);
					} catch (RuntimeException exception) {
						logger.error("Analysis job {} was committed but could not be published; reconciliation will retry it",
								analysisJobId, exception);
					}
				}
			});
			return;
		}
		publish(analysisJobId);
	}

	private void publish(UUID analysisJobId) {
		redisTemplate.opsForList().rightPush(queueName, analysisJobId.toString());
	}
}