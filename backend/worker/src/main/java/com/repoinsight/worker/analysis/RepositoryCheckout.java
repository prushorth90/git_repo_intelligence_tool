package com.repoinsight.worker.analysis;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

public final class RepositoryCheckout implements AutoCloseable {

	private final Path directory;

	public RepositoryCheckout(Path directory) {
		this.directory = directory;
	}

	public Path directory() {
		return directory;
	}

	@Override
	public void close() {
		if (!Files.exists(directory)) return;
		try (var paths = Files.walk(directory)) {
			paths.sorted(Comparator.reverseOrder()).forEach(path -> {
				try {
					Files.deleteIfExists(path);
				} catch (IOException exception) {
					throw new RepositoryCleanupException(path, exception);
				}
			});
		} catch (IOException exception) {
			throw new RepositoryCleanupException(directory, exception);
		}
	}
}