package com.repoinsight.api.repository;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidRepositoryUrlException extends RuntimeException {

	public InvalidRepositoryUrlException(String message) {
		super(message);
	}
}