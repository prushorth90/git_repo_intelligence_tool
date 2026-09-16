package com.repoinsight.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app")
public record AppProperties(Cors cors) {

	public record Cors(String allowedOrigin) {
	}
}