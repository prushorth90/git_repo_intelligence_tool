package com.repoinsight.api.infrastructure.github;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.time.Instant;

import com.repoinsight.api.service.GitHubRepositoryPage;
import com.repoinsight.api.service.exception.GitHubRateLimitException;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class GitHubRestClientTests {

	@Test
	void mapsRepositoryPageAndPaginationHeaders() {
		RestClient.Builder builder = RestClient.builder();
		MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
		GitHubRestClient client = new GitHubRestClient(builder);
		server.expect(request -> assertThat(request.getURI().getQuery())
				.contains("page=2", "per_page=25", "visibility=all"))
				.andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer token"))
				.andRespond(withSuccess("""
						[{
						  "id": 99,
						  "owner": {"login": "octocat"},
						  "name": "hello-world",
						  "full_name": "octocat/hello-world",
						  "html_url": "https://github.com/octocat/hello-world",
						  "default_branch": "trunk",
						  "visibility": "private",
						  "private": true,
						  "language": "Java",
						  "stargazers_count": 120,
						  "forks_count": 14,
						  "updated_at": "2026-09-01T10:15:30Z"
						}]
						""", MediaType.APPLICATION_JSON)
						.header(HttpHeaders.LINK, "<https://api.github.com/user/repos?page=3>; rel=\"next\"")
						.header("X-RateLimit-Remaining", "4180")
						.header("X-RateLimit-Reset", "1788257730"));

		GitHubRepositoryPage result = client.findRepositories("token", 2, 25);

		assertThat(result.hasNextPage()).isTrue();
		assertThat(result.rateLimitRemaining()).isEqualTo(4180);
		assertThat(result.rateLimitResetAt()).isEqualTo(Instant.ofEpochSecond(1788257730));
		assertThat(result.repositories()).singleElement().satisfies(repository -> {
			assertThat(repository.id()).isEqualTo(99L);
			assertThat(repository.fullName()).isEqualTo("octocat/hello-world");
			assertThat(repository.primaryLanguage()).isEqualTo("Java");
			assertThat(repository.stars()).isEqualTo(120);
			assertThat(repository.forks()).isEqualTo(14);
		});
		server.verify();
	}

	@Test
	void convertsExhaustedGitHubRateLimitToDomainException() {
		RestClient.Builder builder = RestClient.builder();
		MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
		GitHubRestClient client = new GitHubRestClient(builder);
		server.expect(request -> { })
				.andRespond(withStatus(HttpStatus.FORBIDDEN)
						.header("X-RateLimit-Remaining", "0")
						.header("X-RateLimit-Reset", "1788257730"));

		assertThatThrownBy(() -> client.findRepositories("token", 1, 30))
				.isInstanceOf(GitHubRateLimitException.class)
				.hasMessageContaining("2026");
		server.verify();
	}
}