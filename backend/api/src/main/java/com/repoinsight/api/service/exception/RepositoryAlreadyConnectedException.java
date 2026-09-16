package com.repoinsight.api.service.exception;

public class RepositoryAlreadyConnectedException extends RuntimeException {

	public RepositoryAlreadyConnectedException(String fullName) {
		super("Repository is already connected: " + fullName);
	}
}