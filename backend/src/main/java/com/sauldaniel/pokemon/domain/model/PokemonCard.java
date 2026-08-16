package com.sauldaniel.pokemon.domain.model;

import java.util.List;

public record PokemonCard(
		String name,
		String spriteUrl,
		String category,
		double weightKg,
		List<String> abilities
) {
}
