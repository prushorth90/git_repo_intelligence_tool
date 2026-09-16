package com.repoinsight.worker.analysis;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.patch.FileHeader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.EmptyTreeIterator;
import org.eclipse.jgit.util.io.DisabledOutputStream;
import org.springframework.stereotype.Component;

@Component
public class GitHistoryAnalyzer {

	public GitHistoryAnalysisResult analyze(Path checkout, UUID jobId, Predicate<UUID> cancelled) {
		Instant now = Instant.now();
		Map<HistoryPeriod, Map<String, MutableMetric>> metrics = new EnumMap<>(HistoryPeriod.class);
		for (HistoryPeriod period : HistoryPeriod.values()) metrics.put(period, new HashMap<>());

		try (Git git = Git.open(checkout.toFile());
				Repository repository = git.getRepository();
				DiffFormatter formatter = new DiffFormatter(DisabledOutputStream.INSTANCE)) {
			formatter.setRepository(repository);
			formatter.setDetectRenames(true);
			for (RevCommit commit : git.log().all().call()) {
				if (cancelled.test(jobId)) throw new AnalysisCancelledException();
				Instant committedAt = commit.getAuthorIdent().getWhenAsInstant();
				List<HistoryPeriod> periods = periodsFor(committedAt, now);
				String contributor = contributor(commit);
				for (DiffEntry diff : formatter.scan(parentTree(repository, commit), tree(repository, commit))) {
					String path = diff.getChangeType() == DiffEntry.ChangeType.DELETE ? diff.getOldPath() : diff.getNewPath();
					if (!SourceTreeAnalyzer.isRelevantSourcePath(path)) continue;
					FileHeader header = formatter.toFileHeader(diff);
					if (header.getPatchType() == FileHeader.PatchType.BINARY) continue;
					int additions = 0;
					int deletions = 0;
					for (Edit edit : header.toEditList()) {
						additions += edit.getEndB() - edit.getBeginB();
						deletions += edit.getEndA() - edit.getBeginA();
					}
					for (HistoryPeriod period : periods) {
						metrics.get(period).computeIfAbsent(path, key -> new MutableMetric(path))
								.add(additions, deletions, contributor, committedAt);
					}
				}
			}
		} catch (IOException | GitAPIException exception) {
			throw new IllegalStateException("Git history analysis failed.", exception);
		}

		Map<HistoryPeriod, List<FileHistoryMetric>> result = new EnumMap<>(HistoryPeriod.class);
		metrics.forEach((period, values) -> result.put(period, values.values().stream()
				.map(MutableMetric::toMetric)
				.sorted((left, right) -> Long.compare(right.totalChurn(), left.totalChurn()))
				.toList()));
		return new GitHistoryAnalysisResult(Map.copyOf(result));
	}

	private List<HistoryPeriod> periodsFor(Instant commit, Instant now) {
		List<HistoryPeriod> periods = new ArrayList<>();
		periods.add(HistoryPeriod.ALL);
		if (!commit.isBefore(now.minus(180, ChronoUnit.DAYS))) periods.add(HistoryPeriod.MONTHS_6);
		if (!commit.isBefore(now.minus(90, ChronoUnit.DAYS))) periods.add(HistoryPeriod.DAYS_90);
		if (!commit.isBefore(now.minus(30, ChronoUnit.DAYS))) periods.add(HistoryPeriod.DAYS_30);
		return periods;
	}

	private String contributor(RevCommit commit) {
		String email = commit.getAuthorIdent().getEmailAddress();
		return email == null || email.isBlank() ? commit.getAuthorIdent().getName() : email;
	}

	private AbstractTreeIterator parentTree(Repository repository, RevCommit commit) throws IOException {
		if (commit.getParentCount() == 0) return new EmptyTreeIterator();
		try (RevWalk walk = new RevWalk(repository); ObjectReader reader = repository.newObjectReader()) {
			RevCommit parent = walk.parseCommit(commit.getParent(0).getId());
			CanonicalTreeParser parser = new CanonicalTreeParser();
			parser.reset(reader, parent.getTree().getId());
			return parser;
		}
	}

	private AbstractTreeIterator tree(Repository repository, RevCommit commit) throws IOException {
		try (ObjectReader reader = repository.newObjectReader()) {
			CanonicalTreeParser parser = new CanonicalTreeParser();
			parser.reset(reader, commit.getTree().getId());
			return parser;
		}
	}

	private static final class MutableMetric {
		private final String path;
		private final Set<String> contributors = new HashSet<>();
		private int commits;
		private int additions;
		private int deletions;
		private Instant lastModifiedAt;

		private MutableMetric(String path) {
			this.path = path;
		}

		private void add(int added, int removed, String contributor, Instant committedAt) {
			commits++;
			additions += added;
			deletions += removed;
			contributors.add(contributor);
			if (lastModifiedAt == null || committedAt.isAfter(lastModifiedAt)) lastModifiedAt = committedAt;
		}

		private FileHistoryMetric toMetric() {
			return new FileHistoryMetric(path, SourceTreeAnalyzer.languageForPath(path), commits,
					additions, deletions, contributors.size(), lastModifiedAt);
		}
	}
}