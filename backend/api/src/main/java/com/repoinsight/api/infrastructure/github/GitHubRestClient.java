package com.repoinsight.api.infrastructure.github;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.repoinsight.api.service.GitHubClient;
import com.repoinsight.api.service.GitHubRepository;
import com.repoinsight.api.service.GitHubRepositoryPage;
import com.repoinsight.api.service.exception.GitHubApiException;
import com.repoinsight.api.service.exception.GitHubRateLimitException;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class GitHubRestClient implements GitHubClient {

	private static final String API_VERSION = "2022-11-28";
	private static final ParameterizedTypeReference<List<GitHubRepositoryPayload>> REPOSITORY_LIST =
			new ParameterizedTypeReference<>() { };

	private final RestClient restClient;

	public GitHubRestClient(RestClient.Builder builder) {
		this.restClient = builder.baseUrl("https://api.github.com").build();
	}

	@Override
	public GitHubRepositoryPage findRepositories(String accessToken, int page, int perPage) {
		ResponseEntity<List<GitHubRepositoryPayload>> response = restClient.get()
				.uri(uri -> uri.path("/user/repos")
						.queryParam("visibility", "all")
						.queryParam("affiliation", "owner,collaborator,organization_member")
						.queryParam("sort", "updated")
						.queryParam("direction", "desc")
						.queryParam("page", page)
						.queryParam("per_page", perPage)
						.build())
				.headers(headers -> githubHeaders(headers, accessToken))
				.retrieve()
				.onStatus(HttpStatusCode::isError, (request, apiResponse) -> handleError(apiResponse.getStatusCode(), apiResponse.getHeaders()))
				.toEntity(REPOSITORY_LIST);

		List<GitHubRepository> repositories = response.getBody() == null
				? List.of()
				: response.getBody().stream().map(GitHubRepositoryPayload::toDomain).toList();
		HttpHeaders headers = response.getHeaders();
		return new GitHubRepositoryPage(
				repositories,
				page,
				perPage,
				hasNextPage(headers),
				integerHeader(headers, "X-RateLimit-Remaining", -1),
				rateLimitReset(headers));
	}

	@Override
	public GitHubRepository getRepository(String accessToken, long repositoryId) {
		GitHubRepositoryPayload payload = restClient.get()
				.uri("/repositories/{repositoryId}", repositoryId)
				.headers(headers -> githubHeaders(headers, accessToken))
				.retrieve()
				.onStatus(HttpStatusCode::isError, (request, apiResponse) -> handleError(apiResponse.getStatusCode(), apiResponse.getHeaders()))
				.body(GitHubRepositoryPayload.class);
		if (payload == null) throw new IllegalStateException("GitHub returned an empty repository response.");
		return payload.toDomain();
	}

	private void githubHeaders(HttpHeaders headers, String accessToken) {
		headers.setBearerAuth(accessToken);
		headers.set("Accept", "application/vnd.github+json");
		headers.set("X-GitHub-Api-Version", API_VERSION);
	}

	private void handleError(HttpStatusCode status, HttpHeaders headers) {
		if (status.value() == 429 || (status.value() == 403 && integerHeader(headers, "X-RateLimit-Remaining", -1) == 0)) {
			throw new GitHubRateLimitException(rateLimitReset(headers));
		}
		if (status.value() == 401) {
			throw new GitHubApiException("GitHub authorization has expired. Disconnect and reconnect your account.");
		}
		throw new GitHubApiException("GitHub API request failed with status " + status.value() + ".");
	}

	private boolean hasNextPage(HttpHeaders headers) {
		String link = headers.getFirst(HttpHeaders.LINK);
		return link != null && link.contains("rel=\"next\"");
	}

	private int integerHeader(HttpHeaders headers, String name, int fallback) {
		try {
			String value = headers.getFirst(name);
			return value == null ? fallback : Integer.parseInt(value);
		} catch (NumberFormatException exception) {
			return fallback;
		}
	}

	private Instant rateLimitReset(HttpHeaders headers) {
		try {
			String value = headers.getFirst("X-RateLimit-Reset");
			return value == null ? Instant.EPOCH : Instant.ofEpochSecond(Long.parseLong(value));
		} catch (NumberFormatException exception) {
			return Instant.EPOCH;
		}
	}

	private record GitHubRepositoryPayload(
			long id,
			Owner owner,
			String name,
			@JsonProperty("full_name") String fullName,
			@JsonProperty("html_url") String htmlUrl,
			@JsonProperty("default_branch") String defaultBranch,
			String visibility,
			@JsonProperty("private") boolean privateRepository,
			String language,
			@JsonProperty("stargazers_count") int stars,
			@JsonProperty("forks_count") int forks,
			@JsonProperty("updated_at") Instant updatedAt) {

		GitHubRepository toDomain() {
			return new GitHubRepository(id, owner.login(), name, fullName, htmlUrl, defaultBranch,
					visibility, privateRepository, language, stars, forks, updatedAt);
		}
	}

	private record Owner(String login) {
	}
}