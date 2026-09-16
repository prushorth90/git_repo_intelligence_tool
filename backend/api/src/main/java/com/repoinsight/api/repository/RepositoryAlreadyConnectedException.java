package com.repoinsight.api.repository;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class RepositoryAlreadyConnectedException extends RuntimeException {

	public RepositoryAlreadyConnectedException(String fullName) {
		super("Repository is already connected: " + fullName);
	}
}