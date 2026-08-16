package com.sauldaniel.pokemon.config;

import com.sauldaniel.pokemon.domain.model.Role;
import com.sauldaniel.pokemon.domain.model.UserAccount;
import com.sauldaniel.pokemon.domain.port.out.UserRepositoryPort;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class DemoUserSeeder implements ApplicationRunner {

	private final UserRepositoryPort users;
	private final PasswordEncoder passwordEncoder;

	public DemoUserSeeder(UserRepositoryPort users, PasswordEncoder passwordEncoder) {
		this.users = users;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public void run(ApplicationArguments args) {
		seed("admin@example.com", "password123", Role.ADMIN);
		seed("demo@example.com", "password123", Role.USER);
	}

	private void seed(String email, String rawPassword, Role role) {
		if (users.existsByEmail(email)) {
			return;
		}
		users.save(new UserAccount(
				null,
				email,
				passwordEncoder.encode(rawPassword),
				role,
				Instant.now()));
	}
}
