package com.repoinsight.api.service.exception;

public class InvalidRepositoryUrlException extends RuntimeException {

	public InvalidRepositoryUrlException(String message) {
		super(message);
	}
}