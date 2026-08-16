package com.sauldaniel.pokemon.application.service;

import java.util.Set;

public record UpdateLocalPokemonCommand(
		String localizedName,
		String region,
		String internalNotes,
		Set<String> tags,
		long version
) {
}
