package com.sauldaniel.pokemon.domain.model;

import java.util.List;

public record PokemonNamePage(
		List<String> names,
		long totalCount
) {
}
