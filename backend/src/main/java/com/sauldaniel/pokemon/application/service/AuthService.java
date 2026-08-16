package com.sauldaniel.pokemon.application.service;

import com.sauldaniel.pokemon.domain.exception.ConflictException;
import com.sauldaniel.pokemon.domain.model.Role;
import com.sauldaniel.pokemon.domain.model.UserAccount;
import com.sauldaniel.pokemon.domain.port.out.UserRepositoryPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class AuthService {

	private final UserRepositoryPort users;
	private final PasswordEncoder passwordEncoder;
	private final JwtEncoder jwtEncoder;
	private final long tokenTtlSeconds;

	public AuthService(
			UserRepositoryPort users,
			PasswordEncoder passwordEncoder,
			JwtEncoder jwtEncoder,
			@Value("${app.security.jwt-ttl-seconds:3600}") long tokenTtlSeconds) {
		this.users = users;
		this.passwordEncoder = passwordEncoder;
		this.jwtEncoder = jwtEncoder;
		this.tokenTtlSeconds = tokenTtlSeconds;
	}

	@Transactional
	public UserAccount register(String email, String rawPassword) {
		if (users.existsByEmail(email)) {
			throw new ConflictException("Email already registered");
		}
		UserAccount user = new UserAccount(
				null,
				email.trim().toLowerCase(),
				passwordEncoder.encode(rawPassword),
				Role.USER,
				Instant.now());
		return users.save(user);
	}

	@Transactional(readOnly = true)
	public LoginResult login(String email, String rawPassword) {
		UserAccount user = users.findByEmail(email.trim().toLowerCase())
				.orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
		if (!passwordEncoder.matches(rawPassword, user.passwordHash())) {
			throw new BadCredentialsException("Invalid credentials");
		}
		Instant now = Instant.now();
		Instant expiresAt = now.plusSeconds(tokenTtlSeconds);
		JwtClaimsSet claims = JwtClaimsSet.builder()
				.issuer("pokemon-api")
				.issuedAt(now)
				.expiresAt(expiresAt)
				.subject(user.id().toString())
				.claim("email", user.email())
				.claim("role", user.role().name())
				.build();
		String token = jwtEncoder.encode(JwtEncoderParameters.from(
				JwsHeader.with(MacAlgorithm.HS256).build(),
				claims)).getTokenValue();
		return new LoginResult(token, "Bearer", tokenTtlSeconds, user.role(), user.email());
	}

	public record LoginResult(
			String accessToken,
			String tokenType,
			long expiresIn,
			Role role,
			String email) {
	}
}
