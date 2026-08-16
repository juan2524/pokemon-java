package com.sauldaniel.pokemon.domain.model;

import java.util.Set;

public final class AllowedPokemonTags {

	public static final Set<String> ALL = Set.of(
			"starter",
			"legendary",
			"favorite",
			"team",
			"shiny",
			"competitive");

	private AllowedPokemonTags() {
	}

	public static boolean isAllowed(String tag) {
		return tag != null && ALL.contains(tag);
	}
}
