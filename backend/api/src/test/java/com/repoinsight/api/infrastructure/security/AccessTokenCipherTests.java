package com.repoinsight.api.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Base64;
import java.util.List;

import com.repoinsight.api.configuration.AppProperties;

import org.junit.jupiter.api.Test;

class AccessTokenCipherTests {

	@Test
	void encryptsAndDecryptsAccessToken() {
		String key = Base64.getEncoder().encodeToString(new byte[32]);
		AppProperties properties = new AppProperties(
				new AppProperties.Cors(List.of("http://localhost:5173")),
				new AppProperties.Github("client-id", "client-secret", key, "http://localhost:5173"));
		AccessTokenCipher cipher = new AccessTokenCipher(properties);

		String encrypted = cipher.encrypt("github-access-token");

		assertThat(encrypted).doesNotContain("github-access-token");
		assertThat(cipher.decrypt(encrypted)).isEqualTo("github-access-token");
	}
}