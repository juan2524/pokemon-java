package com.sauldaniel.pokemon.adapter.in.web.dto;

import java.util.List;

public record PokemonDetailResponse(
		String name,
		String imageUrl,
		double heightM,
		double weightKg,
		List<String> types,
		List<PokemonStatResponse> stats,
		String flavorTextEn,
		List<EvolutionNodeResponse> evolutionLineage
) {

	public record PokemonStatResponse(String name, int baseValue) {
	}

	public record EvolutionNodeResponse(String name) {
	}
}
