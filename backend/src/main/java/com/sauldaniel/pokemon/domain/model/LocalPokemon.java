package com.sauldaniel.pokemon.domain.model;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record LocalPokemon(
		UUID id,
		int pokeApiId,
		String name,
		String localizedName,
		String region,
		String internalNotes,
		Set<String> tags,
		Instant syncedAt,
		long version
) {
}
