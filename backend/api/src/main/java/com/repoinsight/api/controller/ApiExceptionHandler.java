package com.repoinsight.api.controller;

import java.time.Duration;

import com.repoinsight.api.service.exception.InvalidRepositoryUrlException;
import com.repoinsight.api.service.exception.GitHubOAuthNotConfiguredException;
import com.repoinsight.api.service.exception.GitHubConnectionRequiredException;
import com.repoinsight.api.service.exception.GitHubApiException;
import com.repoinsight.api.service.exception.GitHubRateLimitException;
import com.repoinsight.api.service.exception.RepositoryAlreadyConnectedException;
import com.repoinsight.api.service.exception.RepositoryNotFoundException;
import com.repoinsight.api.service.exception.AnalysisJobAlreadyActiveException;
import com.repoinsight.api.service.exception.AnalysisJobNotCancellableException;
import com.repoinsight.api.service.exception.AnalysisJobNotFoundException;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

	@ExceptionHandler(InvalidRepositoryUrlException.class)
	public ProblemDetail invalidRepositoryUrl(InvalidRepositoryUrlException exception) {
		return problem(HttpStatus.BAD_REQUEST, exception.getMessage());
	}

	@ExceptionHandler(RepositoryAlreadyConnectedException.class)
	public ProblemDetail repositoryAlreadyConnected(RepositoryAlreadyConnectedException exception) {
		return problem(HttpStatus.CONFLICT, exception.getMessage());
	}

	@ExceptionHandler(RepositoryNotFoundException.class)
	public ProblemDetail repositoryNotFound(RepositoryNotFoundException exception) {
		return problem(HttpStatus.NOT_FOUND, exception.getMessage());
	}

	@ExceptionHandler(AnalysisJobNotFoundException.class)
	public ProblemDetail analysisJobNotFound(AnalysisJobNotFoundException exception) {
		return problem(HttpStatus.NOT_FOUND, exception.getMessage());
	}

	@ExceptionHandler({ AnalysisJobAlreadyActiveException.class, AnalysisJobNotCancellableException.class })
	public ProblemDetail analysisJobConflict(RuntimeException exception) {
		return problem(HttpStatus.CONFLICT, exception.getMessage());
	}

	@ExceptionHandler(GitHubOAuthNotConfiguredException.class)
	public ProblemDetail githubOAuthNotConfigured(GitHubOAuthNotConfiguredException exception) {
		return problem(HttpStatus.SERVICE_UNAVAILABLE, exception.getMessage());
	}

	@ExceptionHandler(GitHubConnectionRequiredException.class)
	public ProblemDetail githubConnectionRequired(GitHubConnectionRequiredException exception) {
		return problem(HttpStatus.UNAUTHORIZED, exception.getMessage());
	}

	@ExceptionHandler(GitHubRateLimitException.class)
	public ResponseEntity<ProblemDetail> githubRateLimit(GitHubRateLimitException exception) {
		ProblemDetail problem = problem(HttpStatus.TOO_MANY_REQUESTS, exception.getMessage());
		problem.setProperty("rateLimitResetAt", exception.getResetAt());
		long retryAfter = Math.max(0, Duration.between(java.time.Instant.now(), exception.getResetAt()).toSeconds());
		return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
				.header(HttpHeaders.RETRY_AFTER, Long.toString(retryAfter))
				.body(problem);
	}

	@ExceptionHandler(GitHubApiException.class)
	public ProblemDetail githubApi(GitHubApiException exception) {
		return problem(HttpStatus.BAD_GATEWAY, exception.getMessage());
	}

	private ProblemDetail problem(HttpStatus status, String detail) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
		problem.setTitle(status.getReasonPhrase());
		return problem;
	}
}