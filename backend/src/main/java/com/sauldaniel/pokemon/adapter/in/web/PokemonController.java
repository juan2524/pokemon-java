package com.sauldaniel.pokemon.adapter.in.web;

import com.sauldaniel.pokemon.adapter.in.web.dto.PokemonCardPageResponse;
import com.sauldaniel.pokemon.adapter.in.web.dto.PokemonCardResponse;
import com.sauldaniel.pokemon.adapter.in.web.dto.PokemonDetailResponse;
import com.sauldaniel.pokemon.application.service.GetPokemonDetailService;
import com.sauldaniel.pokemon.application.service.ListPokemonCardsService;
import com.sauldaniel.pokemon.domain.model.PokemonCard;
import com.sauldaniel.pokemon.domain.model.PokemonCardPage;
import com.sauldaniel.pokemon.domain.model.PokemonDetail;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pokemon")
@Validated
public class PokemonController {

	private final ListPokemonCardsService listPokemonCardsService;
	private final GetPokemonDetailService getPokemonDetailService;

	public PokemonController(
			ListPokemonCardsService listPokemonCardsService,
			GetPokemonDetailService getPokemonDetailService) {
		this.listPokemonCardsService = listPokemonCardsService;
		this.getPokemonDetailService = getPokemonDetailService;
	}

	@GetMapping
	public PokemonCardPageResponse list(
			@RequestParam(defaultValue = "0") @Min(0) int page,
			@RequestParam(defaultValue = "20") @Min(1) @Max(50) int size) {
		PokemonCardPage result = listPokemonCardsService.execute(page, size);
		return toResponse(result);
	}

	@GetMapping("/{idOrName}")
	public PokemonDetailResponse detail(@PathVariable String idOrName) {
		return toResponse(getPokemonDetailService.execute(idOrName));
	}

	private static PokemonCardPageResponse toResponse(PokemonCardPage page) {
		return new PokemonCardPageResponse(
				page.content().stream().map(PokemonController::toResponse).toList(),
				page.page(),
				page.size(),
				page.totalElements(),
				page.totalPages());
	}

	private static PokemonCardResponse toResponse(PokemonCard card) {
		return new PokemonCardResponse(
				card.name(),
				card.spriteUrl(),
				card.category(),
				card.weightKg(),
				card.abilities());
	}

	private static PokemonDetailResponse toResponse(PokemonDetail detail) {
		return new PokemonDetailResponse(
				detail.name(),
				detail.imageUrl(),
				detail.heightM(),
				detail.weightKg(),
				detail.types(),
				detail.stats().stream()
						.map(stat -> new PokemonDetailResponse.PokemonStatResponse(stat.name(), stat.baseValue()))
						.toList(),
				detail.flavorTextEn(),
				detail.evolutionLineage().stream()
						.map(node -> new PokemonDetailResponse.EvolutionNodeResponse(node.name()))
						.toList());
	}
}
