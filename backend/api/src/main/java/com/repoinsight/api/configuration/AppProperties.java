package com.repoinsight.api.configuration;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app")
public record AppProperties(Cors cors, Github github) {

	public record Cors(List<String> allowedOrigins) {
	}

	public record Github(String clientId, String clientSecret, String tokenEncryptionKey, String frontendRedirectUrl) {
		public boolean configured() {
			return clientId != null && !clientId.isBlank()
					&& !"github-client-id-not-configured".equals(clientId)
					&& clientSecret != null && !clientSecret.isBlank()
					&& !"github-client-secret-not-configured".equals(clientSecret)
					&& tokenEncryptionKey != null && !tokenEncryptionKey.isBlank();
		}
	}
}