package com.sauldaniel.pokemon.adapter.in.web.dto;

public record LoginResponse(
		String accessToken,
		String tokenType,
		long expiresIn,
		String role,
		String email
) {
}
