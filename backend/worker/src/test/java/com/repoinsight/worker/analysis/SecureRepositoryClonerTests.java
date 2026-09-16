package com.repoinsight.worker.analysis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SecureRepositoryClonerTests {

	@TempDir
	Path temporaryDirectory;

	@Test
	void rejectsNonGithubRepositoryBeforeCreatingTemporaryDirectory() throws Exception {
		SecureRepositoryCloner cloner = new SecureRepositoryCloner(temporaryDirectory.toString());

		assertThatThrownBy(() -> cloner.cloneRepository("file:///tmp/repository", "main", null, false))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("HTTPS GitHub");
		try (var children = Files.list(temporaryDirectory)) {
			assertThat(children).isEmpty();
		}
	}
}