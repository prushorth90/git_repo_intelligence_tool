package com.repoinsight.api.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app")
public record AppProperties(Cors cors) {

	public record Cors(String allowedOrigin) {
	}
}