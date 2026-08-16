package com.sauldaniel.pokemon.domain.model;

import java.time.Instant;
import java.util.UUID;

public record UserAccount(
		UUID id,
		String email,
		String passwordHash,
		Role role,
		Instant createdAt
) {
}
