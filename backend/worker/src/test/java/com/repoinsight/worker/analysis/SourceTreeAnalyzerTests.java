package com.repoinsight.worker.analysis;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SourceTreeAnalyzerTests {

	@TempDir
	Path temporaryDirectory;

	private final SourceTreeAnalyzer analyzer = new SourceTreeAnalyzer();
	private final StructuralSourceAnalyzer structuralAnalyzer = new StructuralSourceAnalyzer(java.util.List.of(
			new JavaSourceCodeParser(), new CSharpSourceCodeParser(), new PythonSourceCodeParser(),
			new TypeScriptSourceCodeParser()));

	@Test
	void collectsTextSourceMetricsAndIgnoresGeneratedAndBinaryContent() throws Exception {
		Path source = Files.createDirectories(temporaryDirectory.resolve("src/main"));
		Files.writeString(source.resolve("Application.java"), "class Application {}", StandardCharsets.UTF_8);
		Files.writeString(source.resolve("client.ts"), "export const value = 1", StandardCharsets.UTF_8);
		Files.writeString(temporaryDirectory.resolve("README.md"), "# Project", StandardCharsets.UTF_8);
		Files.write(source.resolve("image.dat"), new byte[] { 1, 0, 2, 3 });
		Files.writeString(source.resolve("bundle.min.js"), "generated", StandardCharsets.UTF_8);
		Path nodeModules = Files.createDirectories(temporaryDirectory.resolve("node_modules/package"));
		Files.writeString(nodeModules.resolve("ignored.js"), "ignored", StandardCharsets.UTF_8);
		Path target = Files.createDirectories(temporaryDirectory.resolve("target/generated"));
		Files.writeString(target.resolve("Ignored.java"), "class Ignored {}", StandardCharsets.UTF_8);

		SourceAnalysisResult result = analyzer.analyze(temporaryDirectory);

		assertThat(result.sourceFileCount()).isEqualTo(3);
		assertThat(result.languageDistribution()).containsEntry("Java", 1)
				.containsEntry("TypeScript", 1)
				.containsEntry("Markdown", 1);
		assertThat(result.fileExtensions()).containsEntry(".java", 1).containsEntry(".ts", 1).containsEntry(".md", 1);
		assertThat(result.directoryStructure()).contains("src/", "src/main/")
				.noneMatch(path -> path.contains("node_modules") || path.contains("target"));
		assertThat(result.repositorySizeBytes()).isPositive();
	}

	@Test
	void checkoutDeletesEntireTemporaryTree() throws Exception {
		Path checkoutDirectory = Files.createDirectories(temporaryDirectory.resolve("checkout/nested"));
		Files.writeString(checkoutDirectory.resolve("file.java"), "class File {}", StandardCharsets.UTF_8);
		Path root = temporaryDirectory.resolve("checkout");

		new RepositoryCheckout(root).close();

		assertThat(root).doesNotExist();
	}

	@Test
	void structurallyParsesOnlySupportedFiles() throws Exception {
		Files.writeString(temporaryDirectory.resolve("Demo.java"), "class Demo { void run() {} }");
		Files.writeString(temporaryDirectory.resolve("notes.txt"), "not source");

		var results = structuralAnalyzer.analyze(
				temporaryDirectory, java.util.UUID.randomUUID(), (id, progress) -> { }, id -> false);

		assertThat(results).singleElement().satisfies(metric -> {
			assertThat(metric.language()).isEqualTo("Java");
			assertThat(metric.classCount()).isEqualTo(1);
			assertThat(metric.methodCount()).isEqualTo(1);
		});
	}
}