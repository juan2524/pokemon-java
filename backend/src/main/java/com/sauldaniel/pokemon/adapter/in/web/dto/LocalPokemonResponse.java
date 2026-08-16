package com.sauldaniel.pokemon.adapter.in.web.dto;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record LocalPokemonResponse(
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
