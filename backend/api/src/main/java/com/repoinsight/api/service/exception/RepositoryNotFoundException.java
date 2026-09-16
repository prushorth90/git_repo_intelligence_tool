package com.repoinsight.api.service.exception;

import java.util.UUID;

public class RepositoryNotFoundException extends RuntimeException {

	public RepositoryNotFoundException(UUID id) {
		super("Repository was not found: " + id);
	}
}