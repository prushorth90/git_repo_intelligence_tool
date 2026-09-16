package com.repoinsight.worker.analysis;

import java.nio.file.Path;

public class RepositoryCleanupException extends RuntimeException {

	public RepositoryCleanupException(Path path, Throwable cause) {
		super("Could not clean temporary repository path: " + path, cause);
	}
}