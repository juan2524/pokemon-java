package com.sauldaniel.pokemon.adapter.in.web.dto;

import java.util.List;

public record PokemonCardResponse(
		String name,
		String spriteUrl,
		String category,
		double weightKg,
		List<String> abilities
) {
}
