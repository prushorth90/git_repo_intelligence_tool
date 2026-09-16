package com.repoinsight.worker.analysis;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

@Component
public class SourceTreeAnalyzer {

	static final long MAX_ANALYZED_FILE_BYTES = 10 * 1024 * 1024;

	private static final Set<String> IGNORED_DIRECTORIES = Set.of(
			".git", ".idea", ".vscode", ".gradle", ".next", ".nuxt", ".cache", ".terraform",
			".venv", "venv", "__pycache__", "coverage", "pods", "deriveddata",
			"node_modules", "target", "build", "dist", "vendor", "generated", "out", "bin", "obj");
	private static final Set<String> BINARY_EXTENSIONS = Set.of(
			"png", "jpg", "jpeg", "gif", "webp", "ico", "pdf", "zip", "gz", "tar", "jar", "war",
			"class", "so", "dll", "dylib", "exe", "bin", "woff", "woff2", "ttf", "eot", "mp3", "mp4");
	private static final Map<String, String> LANGUAGES = Map.ofEntries(
			Map.entry("java", "Java"), Map.entry("kt", "Kotlin"), Map.entry("kts", "Kotlin"),
			Map.entry("js", "JavaScript"), Map.entry("jsx", "JavaScript"), Map.entry("ts", "TypeScript"),
			Map.entry("tsx", "TypeScript"), Map.entry("py", "Python"), Map.entry("go", "Go"),
			Map.entry("rs", "Rust"), Map.entry("rb", "Ruby"), Map.entry("php", "PHP"),
			Map.entry("cs", "C#"), Map.entry("c", "C"), Map.entry("h", "C/C++"), Map.entry("cpp", "C/C++"),
			Map.entry("cc", "C/C++"), Map.entry("swift", "Swift"), Map.entry("scala", "Scala"),
			Map.entry("sh", "Shell"), Map.entry("sql", "SQL"), Map.entry("html", "HTML"),
			Map.entry("css", "CSS"), Map.entry("scss", "CSS"), Map.entry("vue", "Vue"),
			Map.entry("svelte", "Svelte"), Map.entry("json", "JSON"), Map.entry("yaml", "YAML"),
			Map.entry("yml", "YAML"), Map.entry("xml", "XML"), Map.entry("md", "Markdown"));

	public SourceAnalysisResult analyze(Path root) {
		Accumulator accumulator = new Accumulator(root);
		try {
			Files.walkFileTree(root, accumulator);
		} catch (IOException exception) {
			throw new IllegalStateException("Repository source scan failed.", exception);
		}
		return accumulator.result();
	}

	private static final class Accumulator extends SimpleFileVisitor<Path> {
		private final Path root;
		private final Map<String, Integer> languages = new LinkedHashMap<>();
		private final Map<String, Integer> extensions = new LinkedHashMap<>();
		private final List<String> directories = new ArrayList<>();
		private int sourceFileCount;
		private long sizeBytes;

		private Accumulator(Path root) {
			this.root = root;
		}

		@Override
		public FileVisitResult preVisitDirectory(Path directory, BasicFileAttributes attributes) {
			if (!directory.equals(root) && IGNORED_DIRECTORIES.contains(directory.getFileName().toString().toLowerCase(Locale.ROOT))) {
				return FileVisitResult.SKIP_SUBTREE;
			}
			if (!directory.equals(root) && root.relativize(directory).getNameCount() <= 4 && directories.size() < 5_000) {
				directories.add(normalize(root.relativize(directory)) + "/");
			}
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {
			if (!attributes.isRegularFile() || attributes.size() > MAX_ANALYZED_FILE_BYTES
					|| Files.isSymbolicLink(file) || isGeneratedName(file.getFileName().toString())) return FileVisitResult.CONTINUE;
			String extension = extension(file);
			if (BINARY_EXTENSIONS.contains(extension) || isBinary(file)) return FileVisitResult.CONTINUE;
			sizeBytes += attributes.size();
			extensions.merge(extension.isBlank() ? "[no extension]" : "." + extension, 1, Integer::sum);
			String language = LANGUAGES.get(extension);
			if (language != null) {
				sourceFileCount++;
				languages.merge(language, 1, Integer::sum);
			}
			return FileVisitResult.CONTINUE;
		}

		private SourceAnalysisResult result() {
			return new SourceAnalysisResult(sourceFileCount, sizeBytes, Map.copyOf(languages), List.copyOf(directories), Map.copyOf(extensions));
		}
	}

	static boolean isRelevantSourcePath(String relativePath) {
		String normalized = relativePath.replace('\\', '/');
		String[] parts = normalized.split("/");
		for (int index = 0; index < parts.length - 1; index++) {
			if (IGNORED_DIRECTORIES.contains(parts[index].toLowerCase(Locale.ROOT))) return false;
		}
		String fileName = parts[parts.length - 1];
		String extension = extension(Path.of(fileName));
		return !isGeneratedName(fileName) && !BINARY_EXTENSIONS.contains(extension) && LANGUAGES.containsKey(extension);
	}

	static boolean isIgnoredDirectory(Path directory) {
		return IGNORED_DIRECTORIES.contains(directory.getFileName().toString().toLowerCase(Locale.ROOT));
	}

	static String languageForPath(String relativePath) {
		String normalized = relativePath.replace('\\', '/');
		return LANGUAGES.get(extension(Path.of(normalized).getFileName()));
	}

	private static boolean isGeneratedName(String fileName) {
		String name = fileName.toLowerCase(Locale.ROOT);
		return name.endsWith(".min.js") || name.endsWith(".min.css") || name.endsWith(".map")
				|| name.endsWith(".lock") || name.endsWith("-lock.json");
	}

	static boolean isBinary(Path file) throws IOException {
		try (InputStream input = Files.newInputStream(file)) {
			byte[] sample = input.readNBytes(8_192);
			for (byte value : sample) if (value == 0) return true;
			return false;
		}
	}

	private static String extension(Path file) {
		String name = file.getFileName().toString();
		int separator = name.lastIndexOf('.');
		return separator < 1 || separator == name.length() - 1
				? ""
				: name.substring(separator + 1).toLowerCase(Locale.ROOT);
	}

	private static String normalize(Path path) {
		return path.toString().replace('\\', '/');
	}
}