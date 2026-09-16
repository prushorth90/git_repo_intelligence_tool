package com.repoinsight.worker.analysis;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SecureRepositoryCloner {

	private final Path temporaryRoot;

	public SecureRepositoryCloner(@Value("${app.analysis.temp-directory:}") String temporaryDirectory) {
		this.temporaryRoot = temporaryDirectory == null || temporaryDirectory.isBlank()
				? Path.of(System.getProperty("java.io.tmpdir"))
				: Path.of(temporaryDirectory);
	}

	public RepositoryCheckout cloneRepository(
			String repositoryUrl,
			String defaultBranch,
			String accessToken,
			boolean includeHistory) {
		Path directory = null;
		try {
			validateRepositoryUrl(repositoryUrl);
			Files.createDirectories(temporaryRoot);
			directory = Files.createTempDirectory(temporaryRoot, "repo-analysis-");
			setOwnerOnlyPermissions(directory);
			var command = Git.cloneRepository()
					.setURI(repositoryUrl)
					.setDirectory(directory.toFile())
					.setCloneAllBranches(includeHistory);
			if (!includeHistory) command.setDepth(1);
			if (defaultBranch != null && !defaultBranch.isBlank()) command.setBranch(defaultBranch);
			if (accessToken != null && !accessToken.isBlank()) {
				command.setCredentialsProvider(new UsernamePasswordCredentialsProvider("x-access-token", accessToken));
			}
			try (Git ignored = command.call()) {
				return new RepositoryCheckout(directory);
			}
		} catch (IOException | GitAPIException exception) {
			if (directory != null) new RepositoryCheckout(directory).close();
			throw new IllegalStateException("Repository clone failed.", exception);
		}
	}

	private void validateRepositoryUrl(String repositoryUrl) {
		URI uri = URI.create(repositoryUrl);
		if (!"https".equalsIgnoreCase(uri.getScheme()) || !"github.com".equalsIgnoreCase(uri.getHost())) {
			throw new IllegalArgumentException("Only HTTPS GitHub repository URLs can be cloned.");
		}
	}

	private void setOwnerOnlyPermissions(Path directory) throws IOException {
		try {
			Files.setPosixFilePermissions(directory, Set.of(
					PosixFilePermission.OWNER_READ,
					PosixFilePermission.OWNER_WRITE,
					PosixFilePermission.OWNER_EXECUTE));
		} catch (UnsupportedOperationException ignored) {
			// Non-POSIX filesystems still use the secure platform temp-directory defaults.
		}
	}
}