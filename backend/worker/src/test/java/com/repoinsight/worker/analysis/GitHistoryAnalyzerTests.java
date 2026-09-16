package com.repoinsight.worker.analysis;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.PersonIdent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GitHistoryAnalyzerTests {

	@TempDir
	Path repositoryDirectory;

	@Test
	void aggregatesFileHistoryAcrossConfiguredPeriodsAndIgnoresDependencies() throws Exception {
		try (Git git = Git.init().setDirectory(repositoryDirectory.toFile()).call()) {
			Path source = Files.createDirectories(repositoryDirectory.resolve("src"));
			Files.writeString(source.resolve("App.java"), "one\ntwo\n");
			commit(git, "Initial source", "Alice", "alice@example.com", Instant.now().minus(200, ChronoUnit.DAYS));

			Files.writeString(source.resolve("App.java"), "one\nthree\nfour\n");
			Path dependency = Files.createDirectories(repositoryDirectory.resolve("node_modules/pkg"));
			Files.writeString(dependency.resolve("Ignored.java"), "ignored\n");
			commit(git, "Recent source", "Bob", "bob@example.com", Instant.now().minus(10, ChronoUnit.DAYS));
		}

		GitHistoryAnalysisResult result = new GitHistoryAnalyzer().analyze(
				repositoryDirectory, UUID.randomUUID(), id -> false);

		assertThat(result.metricsByPeriod().get(HistoryPeriod.ALL)).singleElement().satisfies(metric -> {
			assertThat(metric.filePath()).isEqualTo("src/App.java");
			assertThat(metric.commitCount()).isEqualTo(2);
			assertThat(metric.additions()).isEqualTo(4);
			assertThat(metric.deletions()).isEqualTo(1);
			assertThat(metric.uniqueContributors()).isEqualTo(2);
			assertThat(metric.totalChurn()).isEqualTo(5);
		});
		assertThat(result.metricsByPeriod().get(HistoryPeriod.DAYS_30)).singleElement().satisfies(metric -> {
			assertThat(metric.commitCount()).isEqualTo(1);
			assertThat(metric.additions()).isEqualTo(2);
			assertThat(metric.deletions()).isEqualTo(1);
			assertThat(metric.uniqueContributors()).isEqualTo(1);
			assertThat(metric.lastModifiedAt()).isAfter(Instant.now().minus(30, ChronoUnit.DAYS));
		});
	}

	private void commit(Git git, String message, String name, String email, Instant instant) throws Exception {
		PersonIdent author = new PersonIdent(name, email, instant, ZoneOffset.UTC);
		git.add().addFilepattern(".").call();
		git.commit().setMessage(message).setAuthor(author).setCommitter(author).call();
	}
}