package com.repoinsight.api.configuration;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app")
public record AppProperties(Cors cors) {

	public record Cors(List<String> allowedOrigins) {
	}
}