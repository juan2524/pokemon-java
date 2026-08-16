package com.sauldaniel.pokemon.adapter.in.web;

import com.sauldaniel.pokemon.adapter.in.web.dto.LocalPokemonResponse;
import com.sauldaniel.pokemon.application.service.SyncPokemonService;
import com.sauldaniel.pokemon.domain.model.LocalPokemon;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pokemon")
public class PokemonSyncController {

	private final SyncPokemonService syncPokemonService;

	public PokemonSyncController(SyncPokemonService syncPokemonService) {
		this.syncPokemonService = syncPokemonService;
	}

	@PostMapping("/{pokeApiId}/sync")
	public LocalPokemonResponse sync(@PathVariable int pokeApiId) {
		return toResponse(syncPokemonService.execute(pokeApiId));
	}

	private static LocalPokemonResponse toResponse(LocalPokemon pokemon) {
		return new LocalPokemonResponse(
				pokemon.id(),
				pokemon.pokeApiId(),
				pokemon.name(),
				pokemon.localizedName(),
				pokemon.region(),
				pokemon.internalNotes(),
				pokemon.tags(),
				pokemon.syncedAt(),
				pokemon.version());
	}
}
