package com.sauldaniel.pokemon.adapter.in.web;

import com.sauldaniel.pokemon.adapter.in.web.dto.LocalPokemonResponse;
import com.sauldaniel.pokemon.adapter.in.web.dto.PatchLocalPokemonRequest;
import com.sauldaniel.pokemon.application.service.DeleteLocalPokemonService;
import com.sauldaniel.pokemon.application.service.GetLocalPokemonService;
import com.sauldaniel.pokemon.application.service.ListLocalPokemonService;
import com.sauldaniel.pokemon.application.service.UpdateLocalPokemonCommand;
import com.sauldaniel.pokemon.application.service.UpdateLocalPokemonService;
import com.sauldaniel.pokemon.domain.model.LocalPokemon;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/local/pokemon")
public class LocalPokemonController {

	private final ListLocalPokemonService listLocalPokemonService;
	private final GetLocalPokemonService getLocalPokemonService;
	private final UpdateLocalPokemonService updateLocalPokemonService;
	private final DeleteLocalPokemonService deleteLocalPokemonService;

	public LocalPokemonController(
			ListLocalPokemonService listLocalPokemonService,
			GetLocalPokemonService getLocalPokemonService,
			UpdateLocalPokemonService updateLocalPokemonService,
			DeleteLocalPokemonService deleteLocalPokemonService) {
		this.listLocalPokemonService = listLocalPokemonService;
		this.getLocalPokemonService = getLocalPokemonService;
		this.updateLocalPokemonService = updateLocalPokemonService;
		this.deleteLocalPokemonService = deleteLocalPokemonService;
	}

	@GetMapping
	public List<LocalPokemonResponse> list() {
		return listLocalPokemonService.execute().stream().map(LocalPokemonController::toResponse).toList();
	}

	@GetMapping("/{id}")
	public LocalPokemonResponse get(@PathVariable UUID id) {
		return toResponse(getLocalPokemonService.execute(id));
	}

	@PatchMapping("/{id}")
	public LocalPokemonResponse patch(
			@PathVariable UUID id,
			@Valid @RequestBody PatchLocalPokemonRequest request) {
		UpdateLocalPokemonCommand command = new UpdateLocalPokemonCommand(
				request.localizedName(),
				request.region(),
				request.internalNotes(),
				request.tags() == null ? java.util.Set.of() : new LinkedHashSet<>(request.tags()),
				request.version());
		return toResponse(updateLocalPokemonService.execute(id, command));
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable UUID id) {
		deleteLocalPokemonService.execute(id);
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
