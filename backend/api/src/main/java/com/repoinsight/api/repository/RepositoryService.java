package com.repoinsight.api.repository;

import java.net.URI;
import java.util.List;

import com.repoinsight.api.analysis.AnalysisJobPublisher;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RepositoryService {

	private final ConnectedRepositoryRepository repository;
	private final AnalysisJobPublisher jobPublisher;

	public RepositoryService(ConnectedRepositoryRepository repository, AnalysisJobPublisher jobPublisher) {
		this.repository = repository;
		this.jobPublisher = jobPublisher;
	}

	@Transactional(readOnly = true)
	@Cacheable("repositories")
	public List<ConnectedRepository> findAll() {
		return repository.findAll();
	}

	@Transactional
	@CacheEvict(value = "repositories", allEntries = true)
	public ConnectedRepository connect(String githubUrl) {
		RepositoryCoordinates coordinates = parse(githubUrl);
		if (repository.existsByFullNameIgnoreCase(coordinates.fullName())) {
			throw new RepositoryAlreadyConnectedException(coordinates.fullName());
		}

		ConnectedRepository connectedRepository = repository.save(new ConnectedRepository(
				coordinates.owner(), coordinates.name(), coordinates.canonicalUrl()));
		jobPublisher.publish(connectedRepository.getId());
		return connectedRepository;
	}

	RepositoryCoordinates parse(String value) {
		try {
			URI uri = URI.create(value.trim());
			if (!"https".equalsIgnoreCase(uri.getScheme()) || !"github.com".equalsIgnoreCase(uri.getHost())) {
				throw new IllegalArgumentException("Repository URL must use https://github.com.");
			}

			String path = uri.getPath().replaceFirst("^/", "").replaceFirst("\\.git/?$", "");
			String[] segments = path.split("/");
			if (segments.length != 2 || segments[0].isBlank() || segments[1].isBlank()) {
				throw new IllegalArgumentException("Repository URL must identify an owner and repository.");
			}

			return new RepositoryCoordinates(
					segments[0], segments[1], "https://github.com/" + segments[0] + "/" + segments[1]);
		} catch (IllegalArgumentException exception) {
			throw new InvalidRepositoryUrlException(exception.getMessage());
		}
	}

	record RepositoryCoordinates(String owner, String name, String canonicalUrl) {
		String fullName() {
			return owner + "/" + name;
		}
	}
}