package com.repoinsight.worker.analysis;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.regex.Pattern;

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

	private static final double CHURN_WEIGHT = 0.50;
	private static final double TOUCH_WEIGHT = 0.30;
	private static final double RECENCY_WEIGHT = 0.20;
	private static final double CONCENTRATION_THRESHOLD = 70.0;
	private static final Pattern BUG_FIX_MESSAGE = Pattern.compile(
			"(?i)\\b(?:bug(?:fix)?|defect|fix(?:e[ds])?|hotfix|patch|regression|resolve[ds]?)\\b");

	public GitHistoryAnalysisResult analyze(Path checkout, UUID jobId, Predicate<UUID> cancelled) {
		Instant now = Instant.now();
		Map<HistoryPeriod, Map<String, MutableFileMetric>> metrics = new EnumMap<>(HistoryPeriod.class);
		Map<String, MutableContributor> contributors = new HashMap<>();
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
				ContributorIdentity contributor = contributor(commit);
				boolean bugFix = BUG_FIX_MESSAGE.matcher(commit.getFullMessage()).find();
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
					double recency = recencyWeight(committedAt, now);
					for (HistoryPeriod period : periods) {
						metrics.get(period).computeIfAbsent(path, MutableFileMetric::new)
								.add(additions, deletions, contributor, committedAt, recency, bugFix);
					}
					contributors.computeIfAbsent(contributor.key(), key -> new MutableContributor(contributor))
							.add(path, commit.getName(), additions, deletions, committedAt);
				}
			}
		} catch (IOException | GitAPIException exception) {
			throw new IllegalStateException("Git history analysis failed.", exception);
		}

		Map<HistoryPeriod, List<FileHistoryMetric>> periodResults = new EnumMap<>(HistoryPeriod.class);
		metrics.forEach((period, values) -> periodResults.put(period, values.values().stream()
				.map(MutableFileMetric::toMetric)
				.sorted(Comparator.comparingLong(FileHistoryMetric::totalChurn).reversed())
				.toList()));

		Map<String, Double> ownershipWeights = new HashMap<>();
		double totalFileWeight = 0;
		List<FileOwnershipResult> fileOwnership = new ArrayList<>();
		for (MutableFileMetric file : metrics.get(HistoryPeriod.ALL).values()) {
			double fileWeight = Math.max(1, file.totalChurn());
			totalFileWeight += fileWeight;
			for (OwnershipShare share : file.ownershipShares()) {
				ownershipWeights.merge(share.contributor().key(), share.percent() / 100.0 * fileWeight, Double::sum);
				fileOwnership.add(share.toResult(file.path));
			}
		}
		final double ownershipDenominator = totalFileWeight;
		List<ContributorOwnershipResult> contributorResults = contributors.values().stream()
				.map(contributor -> contributor.toResult(
						ownershipDenominator == 0 ? 0 : ownershipWeights.getOrDefault(contributor.identity.key(), 0.0) / ownershipDenominator))
				.sorted(Comparator.comparingDouble(ContributorOwnershipResult::estimatedOwnershipPercent).reversed())
				.toList();

		return new GitHistoryAnalysisResult(Map.copyOf(periodResults), List.copyOf(fileOwnership), contributorResults);
	}

	private List<HistoryPeriod> periodsFor(Instant commit, Instant now) {
		List<HistoryPeriod> periods = new ArrayList<>();
		periods.add(HistoryPeriod.ALL);
		if (!commit.isBefore(now.minus(180, ChronoUnit.DAYS))) periods.add(HistoryPeriod.MONTHS_6);
		if (!commit.isBefore(now.minus(90, ChronoUnit.DAYS))) periods.add(HistoryPeriod.DAYS_90);
		if (!commit.isBefore(now.minus(30, ChronoUnit.DAYS))) periods.add(HistoryPeriod.DAYS_30);
		return periods;
	}

	private double recencyWeight(Instant commit, Instant now) {
		long ageDays = Math.max(0, Duration.between(commit, now).toDays());
		if (ageDays <= 30) return 1.0;
		if (ageDays <= 90) return 0.75;
		if (ageDays <= 180) return 0.5;
		return 0.25;
	}

	private ContributorIdentity contributor(RevCommit commit) {
		String email = commit.getAuthorIdent().getEmailAddress();
		String name = commit.getAuthorIdent().getName();
		String key = email == null || email.isBlank() ? name : email.toLowerCase(Locale.ROOT);
		return new ContributorIdentity(key, name == null || name.isBlank() ? key : name);
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

	private static String module(String path) {
		int separator = path.indexOf('/');
		return separator < 0 ? "[root]" : path.substring(0, separator);
	}

	private record ContributorIdentity(String key, String name) {
	}

	private record OwnershipShare(ContributorIdentity contributor, ContributorActivity activity, double percent) {
		FileOwnershipResult toResult(String path) {
			return new FileOwnershipResult(path, contributor.key(), contributor.name(), activity.commits,
					activity.additions, activity.deletions, activity.lastModifiedAt, percent);
		}
	}

	private static final class ContributorActivity {
		private final ContributorIdentity identity;
		private int commits;
		private int additions;
		private int deletions;
		private double recencyTouches;
		private Instant lastModifiedAt;

		private ContributorActivity(ContributorIdentity identity) {
			this.identity = identity;
		}

		private void add(int added, int removed, Instant committedAt, double recency) {
			commits++;
			additions += added;
			deletions += removed;
			recencyTouches += recency;
			if (lastModifiedAt == null || committedAt.isAfter(lastModifiedAt)) lastModifiedAt = committedAt;
		}

		private long churn() {
			return (long) additions + deletions;
		}
	}

	private static final class MutableFileMetric {
		private final String path;
		private final Map<String, ContributorActivity> contributors = new HashMap<>();
		private int commits;
		private int additions;
		private int deletions;
		private int bugFixCommits;
		private Instant lastModifiedAt;

		private MutableFileMetric(String path) {
			this.path = path;
		}

		private void add(int added, int removed, ContributorIdentity contributor, Instant committedAt, double recency,
				boolean bugFix) {
			commits++;
			additions += added;
			deletions += removed;
			if (bugFix) bugFixCommits++;
			contributors.computeIfAbsent(contributor.key(), key -> new ContributorActivity(contributor))
					.add(added, removed, committedAt, recency);
			if (lastModifiedAt == null || committedAt.isAfter(lastModifiedAt)) lastModifiedAt = committedAt;
		}

		private long totalChurn() {
			return (long) additions + deletions;
		}

		private List<OwnershipShare> ownershipShares() {
			double totalChurn = contributors.values().stream().mapToDouble(ContributorActivity::churn).sum();
			double totalTouches = contributors.values().stream().mapToDouble(activity -> activity.commits).sum();
			double totalRecency = contributors.values().stream().mapToDouble(activity -> activity.recencyTouches).sum();
			int contributorCount = contributors.size();
			return contributors.values().stream().map(activity -> {
				double churnShare = totalChurn == 0 ? 1.0 / contributorCount : activity.churn() / totalChurn;
				double touchShare = totalTouches == 0 ? 1.0 / contributorCount : activity.commits / totalTouches;
				double recencyShare = totalRecency == 0 ? 1.0 / contributorCount : activity.recencyTouches / totalRecency;
				return new OwnershipShare(activity.identity, activity,
						100.0 * (CHURN_WEIGHT * churnShare + TOUCH_WEIGHT * touchShare + RECENCY_WEIGHT * recencyShare));
			}).sorted(Comparator.comparingDouble(OwnershipShare::percent).reversed()).toList();
		}

		private FileHistoryMetric toMetric() {
			List<OwnershipShare> shares = ownershipShares();
			double cumulative = 0;
			int busFactor = 0;
			for (OwnershipShare share : shares) {
				cumulative += share.percent();
				busFactor++;
				if (cumulative >= 50.0) break;
			}
			OwnershipShare top = shares.isEmpty() ? null : shares.get(0);
			return new FileHistoryMetric(path, SourceTreeAnalyzer.languageForPath(path), commits,
					additions, deletions, contributors.size(), bugFixCommits, lastModifiedAt,
					top == null ? null : top.contributor().name(), top == null ? 0 : top.percent(),
					busFactor, top != null && top.percent() >= CONCENTRATION_THRESHOLD);
		}
	}

	private static final class MutableContributor {
		private final ContributorIdentity identity;
		private final Set<String> commits = new HashSet<>();
		private final Set<String> files = new HashSet<>();
		private final Map<String, Long> moduleWeights = new HashMap<>();
		private int additions;
		private int deletions;
		private Instant lastActivityAt;

		private MutableContributor(ContributorIdentity identity) {
			this.identity = identity;
		}

		private void add(String path, String commitId, int added, int removed, Instant committedAt) {
			commits.add(commitId);
			files.add(path);
			additions += added;
			deletions += removed;
			moduleWeights.merge(module(path), Math.max(1L, (long) added + removed), Long::sum);
			if (lastActivityAt == null || committedAt.isAfter(lastActivityAt)) lastActivityAt = committedAt;
		}

		private ContributorOwnershipResult toResult(double ownership) {
			List<String> modules = moduleWeights.entrySet().stream()
					.sorted(Map.Entry.<String, Long>comparingByValue().reversed())
					.limit(3)
					.map(Map.Entry::getKey)
					.toList();
			return new ContributorOwnershipResult(identity.key(), identity.name(), commits.size(), files.size(),
					additions, deletions, ownership * 100.0, ownership, lastActivityAt, modules);
		}
	}
}