package com.repoinsight.worker.analysis;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.RedisListCommands.Direction;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AnalysisJobConsumer {

	private static final Logger logger = LoggerFactory.getLogger(AnalysisJobConsumer.class);

	private final StringRedisTemplate redisTemplate;
	private final AnalysisJobProcessor processor;
	private final String queueName;
	private final String processingQueueName;

	public AnalysisJobConsumer(
			StringRedisTemplate redisTemplate,
			AnalysisJobProcessor processor,
			@Value("${app.analysis.queue-name}") String queueName,
			@Value("${app.analysis.processing-queue-name}") String processingQueueName) {
		this.redisTemplate = redisTemplate;
		this.processor = processor;
		this.queueName = queueName;
		this.processingQueueName = processingQueueName;
	}

	@Scheduled(initialDelay = 5_000, fixedDelayString = "${app.analysis.poll-delay-ms}")
	public void poll() {
		String message = redisTemplate.opsForList().move(
				queueName, Direction.LEFT, processingQueueName, Direction.RIGHT);
		if (message == null) return;

		try {
			processor.process(UUID.fromString(message));
		} catch (IllegalArgumentException exception) {
			logger.error("Discarding malformed analysis job message {}", message, exception);
		} finally {
			redisTemplate.opsForList().remove(processingQueueName, 1, message);
		}
	}
}