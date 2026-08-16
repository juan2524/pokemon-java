package com.sauldaniel.pokemon.domain.model;

import java.util.List;

public record PokemonDetail(
		String name,
		String imageUrl,
		double heightM,
		double weightKg,
		List<String> types,
		List<PokemonStat> stats,
		String flavorTextEn,
		List<EvolutionNode> evolutionLineage
) {
}
