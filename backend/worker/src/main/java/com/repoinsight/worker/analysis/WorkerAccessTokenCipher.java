package com.repoinsight.worker.analysis;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class WorkerAccessTokenCipher {

	private final String encodedKey;

	public WorkerAccessTokenCipher(@Value("${app.github.token-encryption-key:}") String encodedKey) {
		this.encodedKey = encodedKey;
	}

	public String decrypt(String encryptedAccessToken) {
		if (encryptedAccessToken == null || encryptedAccessToken.isBlank()) return null;
		try {
			String[] parts = encryptedAccessToken.split("\\.");
			if (parts.length != 3 || !"v1".equals(parts[0])) throw new IllegalArgumentException("Unsupported token format.");
			byte[] key = Base64.getDecoder().decode(encodedKey);
			if (key.length != 32) throw new IllegalArgumentException("Encryption key must decode to 32 bytes.");
			byte[] iv = Base64.getUrlDecoder().decode(parts[1]);
			byte[] encrypted = Base64.getUrlDecoder().decode(parts[2]);
			Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
			cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(128, iv));
			return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
		} catch (GeneralSecurityException | IllegalArgumentException exception) {
			throw new IllegalStateException("GitHub access token decryption failed.", exception);
		}
	}
}