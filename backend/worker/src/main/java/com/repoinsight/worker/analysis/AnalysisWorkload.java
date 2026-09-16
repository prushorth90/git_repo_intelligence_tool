package com.repoinsight.worker.analysis;

import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AnalysisWorkload {

	private final long stepDelayMs;

	public AnalysisWorkload(@Value("${app.analysis.step-delay-ms}") long stepDelayMs) {
		this.stepDelayMs = stepDelayMs;
	}

	public void analyze(UUID jobId, BiConsumer<UUID, Integer> progress, Predicate<UUID> cancelled) {
		for (int percentage : new int[] { 20, 45, 70, 90 }) {
			if (cancelled.test(jobId)) throw new AnalysisCancelledException();
			pause();
			progress.accept(jobId, percentage);
		}
	}

	private void pause() {
		try {
			Thread.sleep(stepDelayMs);
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("Analysis worker was interrupted.", exception);
		}
	}
}