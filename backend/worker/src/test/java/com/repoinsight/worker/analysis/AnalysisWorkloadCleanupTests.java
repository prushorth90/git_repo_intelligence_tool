package com.repoinsight.worker.analysis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

class AnalysisWorkloadCleanupTests {

	@TempDir
	Path temporaryDirectory;

	@Test
	void deletesCheckoutWhenSourceAnalysisFails() throws Exception {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(new DriverManagerDataSource(
				"jdbc:h2:mem:workload;MODE=PostgreSQL;DB_CLOSE_DELAY=-1", "sa", ""));
		jdbcTemplate.execute("CREATE TABLE connected_repositories (id UUID PRIMARY KEY, github_url VARCHAR, default_branch VARCHAR, github_connection_id UUID)");
		jdbcTemplate.execute("CREATE TABLE github_connections (id UUID PRIMARY KEY, encrypted_access_token VARCHAR)");
		jdbcTemplate.execute("CREATE TABLE analysis_jobs (id UUID PRIMARY KEY, repository_id UUID, include_history BOOLEAN)");
		UUID repositoryId = UUID.randomUUID();
		UUID jobId = UUID.randomUUID();
		jdbcTemplate.update("INSERT INTO connected_repositories VALUES (?, ?, ?, NULL)",
				repositoryId, "https://github.com/example/repository", "main");
		jdbcTemplate.update("INSERT INTO analysis_jobs VALUES (?, ?, FALSE)", jobId, repositoryId);

		Path checkoutPath = Files.createDirectories(temporaryDirectory.resolve("checkout/src"));
		Files.writeString(checkoutPath.resolve("File.java"), "class File {}");
		Path checkoutRoot = temporaryDirectory.resolve("checkout");
		SecureRepositoryCloner cloner = mock(SecureRepositoryCloner.class);
		when(cloner.cloneRepository("https://github.com/example/repository", "main", null, false))
				.thenReturn(new RepositoryCheckout(checkoutRoot));
		SourceTreeAnalyzer analyzer = mock(SourceTreeAnalyzer.class);
		when(analyzer.analyze(checkoutRoot)).thenThrow(new IllegalStateException("scan failed"));
		AnalysisWorkload workload = new AnalysisWorkload(
				jdbcTemplate, cloner, analyzer, mock(StructuralSourceAnalyzer.class), mock(GitHistoryAnalyzer.class),
				new WorkerAccessTokenCipher(""), null);

		assertThatThrownBy(() -> workload.analyze(jobId, (id, progress) -> { }, id -> false))
				.isInstanceOf(IllegalStateException.class)
				.hasMessage("scan failed");
		assertThat(checkoutRoot).doesNotExist();
	}
}