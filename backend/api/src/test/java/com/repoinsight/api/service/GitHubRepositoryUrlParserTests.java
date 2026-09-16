package com.repoinsight.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.repoinsight.api.service.exception.InvalidRepositoryUrlException;

import org.junit.jupiter.api.Test;

class GitHubRepositoryUrlParserTests {

	private final GitHubRepositoryUrlParser parser = new GitHubRepositoryUrlParser();

	@Test
	void parsesAndNormalizesGithubUrl() {
		GitHubRepositoryCoordinates result = parser.parse("https://github.com/spring-projects/spring-boot.git");

		assertThat(result.owner()).isEqualTo("spring-projects");
		assertThat(result.name()).isEqualTo("spring-boot");
		assertThat(result.canonicalUrl()).isEqualTo("https://github.com/spring-projects/spring-boot");
	}

	@Test
	void rejectsNonGithubUrl() {
		assertThatThrownBy(() -> parser.parse("https://example.com/owner/repository"))
				.isInstanceOf(InvalidRepositoryUrlException.class);
	}
}