package com.sauldaniel.pokemon.adapter.in.web.dto;

import java.util.List;

public record PokemonCardPageResponse(
		List<PokemonCardResponse> content,
		int page,
		int size,
		long totalElements,
		int totalPages
) {
}
