package com.sauldaniel.pokemon.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Configuration
public class JwtConfig {

	@Bean
	JwtEncoder jwtEncoder(@Value("${app.security.jwt-secret}") String secret) {
		byte[] key = normalizeSecret(secret);
		return new NimbusJwtEncoder(new ImmutableSecret<>(key));
	}

	@Bean
	JwtDecoder jwtDecoder(@Value("${app.security.jwt-secret}") String secret) {
		byte[] key = normalizeSecret(secret);
		SecretKeySpec secretKey = new SecretKeySpec(key, "HmacSHA256");
		return NimbusJwtDecoder.withSecretKey(secretKey).macAlgorithm(MacAlgorithm.HS256).build();
	}

	private static byte[] normalizeSecret(String secret) {
		byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
		if (bytes.length >= 32) {
			return bytes;
		}
		byte[] padded = new byte[32];
		System.arraycopy(bytes, 0, padded, 0, bytes.length);
		return padded;
	}
}
