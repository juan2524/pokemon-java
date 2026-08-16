package com.sauldaniel.pokemon.domain.model;

import java.util.List;

public record PokemonCardPage(
		List<PokemonCard> content,
		int page,
		int size,
		long totalElements,
		int totalPages
) {
}
