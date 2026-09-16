package com.repoinsight.worker.analysis;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import org.springframework.stereotype.Component;

@Component
public class StructuralSourceAnalyzer {

	private final List<SourceCodeParser> parsers;

	public StructuralSourceAnalyzer(List<SourceCodeParser> parsers) {
		this.parsers = List.copyOf(parsers);
	}

	public List<StructuralFileMetrics> analyze(
			Path repositoryRoot,
			UUID jobId,
			BiConsumer<UUID, Integer> heartbeat,
			Predicate<UUID> cancelled) {
		List<StructuralFileMetrics> results = new ArrayList<>();
		int[] parsedFiles = { 0 };
		try {
			Files.walkFileTree(repositoryRoot, new SimpleFileVisitor<>() {
				@Override
				public FileVisitResult preVisitDirectory(Path directory, BasicFileAttributes attributes) {
					return !directory.equals(repositoryRoot) && SourceTreeAnalyzer.isIgnoredDirectory(directory)
							? FileVisitResult.SKIP_SUBTREE
							: FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {
					if (cancelled.test(jobId)) throw new AnalysisCancelledException();
					if (!attributes.isRegularFile() || attributes.size() > SourceTreeAnalyzer.MAX_ANALYZED_FILE_BYTES
							|| Files.isSymbolicLink(file) || SourceTreeAnalyzer.isBinary(file)) {
						return FileVisitResult.CONTINUE;
					}
					parsers.stream().filter(parser -> parser.supports(file)).findFirst()
							.ifPresent(parser -> {
								results.add(parser.parse(repositoryRoot, file));
								parsedFiles[0]++;
								if (parsedFiles[0] % 50 == 0) heartbeat.accept(jobId, 60);
							});
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException exception) {
			throw new IllegalStateException("Structural source scan failed.", exception);
		}
		return List.copyOf(results);
	}
}