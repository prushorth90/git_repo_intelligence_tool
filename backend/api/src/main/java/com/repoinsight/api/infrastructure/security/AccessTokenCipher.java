package com.repoinsight.api.infrastructure.security;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.repoinsight.api.configuration.AppProperties;

import org.springframework.stereotype.Component;

@Component
public class AccessTokenCipher {

	private static final int GCM_TAG_LENGTH_BITS = 128;
	private static final int IV_LENGTH_BYTES = 12;
	private static final String VERSION = "v1";

	private final AppProperties appProperties;
	private final SecureRandom secureRandom = new SecureRandom();

	public AccessTokenCipher(AppProperties appProperties) {
		this.appProperties = appProperties;
	}

	public String encrypt(String accessToken) {
		try {
			byte[] key = encryptionKey();
			byte[] iv = new byte[IV_LENGTH_BYTES];
			secureRandom.nextBytes(iv);
			Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
			cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
			byte[] encrypted = cipher.doFinal(accessToken.getBytes(StandardCharsets.UTF_8));
			return VERSION + "." + Base64.getUrlEncoder().withoutPadding().encodeToString(iv)
					+ "." + Base64.getUrlEncoder().withoutPadding().encodeToString(encrypted);
		} catch (GeneralSecurityException exception) {
			throw new IllegalStateException("GitHub access token encryption failed.", exception);
		}
	}

	public String decrypt(String encryptedAccessToken) {
		try {
			String[] parts = encryptedAccessToken.split("\\.");
			if (parts.length != 3 || !VERSION.equals(parts[0])) {
				throw new IllegalArgumentException("Unsupported encrypted access token format.");
			}
			byte[] iv = Base64.getUrlDecoder().decode(parts[1]);
			byte[] encrypted = Base64.getUrlDecoder().decode(parts[2]);
			Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
			cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(encryptionKey(), "AES"),
					new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
			return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
		} catch (GeneralSecurityException | IllegalArgumentException exception) {
			throw new IllegalStateException("GitHub access token decryption failed.", exception);
		}
	}

	private byte[] encryptionKey() {
		String encodedKey = appProperties.github().tokenEncryptionKey();
		if (encodedKey == null || encodedKey.isBlank()) {
			throw new IllegalStateException("GITHUB_TOKEN_ENCRYPTION_KEY is required for GitHub OAuth.");
		}
		byte[] key;
		try {
			key = Base64.getDecoder().decode(encodedKey);
		} catch (IllegalArgumentException exception) {
			throw new IllegalStateException("GITHUB_TOKEN_ENCRYPTION_KEY must be Base64 encoded.", exception);
		}
		if (key.length != 32) {
			throw new IllegalStateException("GITHUB_TOKEN_ENCRYPTION_KEY must decode to exactly 32 bytes.");
		}
		return key;
	}
}