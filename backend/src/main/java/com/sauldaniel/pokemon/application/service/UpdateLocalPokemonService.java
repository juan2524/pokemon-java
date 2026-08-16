package com.sauldaniel.pokemon.application.service;

import com.sauldaniel.pokemon.domain.exception.ConflictException;
import com.sauldaniel.pokemon.domain.exception.InvalidTagException;
import com.sauldaniel.pokemon.domain.exception.LocalPokemonNotFoundException;
import com.sauldaniel.pokemon.domain.model.AllowedPokemonTags;
import com.sauldaniel.pokemon.domain.model.LocalPokemon;
import com.sauldaniel.pokemon.domain.port.out.PokemonRecordRepositoryPort;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Service
public class UpdateLocalPokemonService {

	private final PokemonRecordRepositoryPort repository;

	public UpdateLocalPokemonService(PokemonRecordRepositoryPort repository) {
		this.repository = repository;
	}

	@Transactional
	public LocalPokemon execute(UUID id, UpdateLocalPokemonCommand command) {
		LocalPokemon existing = repository.findById(id)
				.orElseThrow(() -> new LocalPokemonNotFoundException(id.toString()));

		Set<String> tags = normalizeTags(command.tags());
		validateTags(tags);

		if (existing.version() != command.version()) {
			throw new ConflictException("Optimistic lock conflict");
		}

		LocalPokemon updated = new LocalPokemon(
				existing.id(),
				existing.pokeApiId(),
				existing.name(),
				command.localizedName(),
				command.region(),
				command.internalNotes(),
				tags,
				existing.syncedAt(),
				existing.version());

		try {
			return repository.save(updated);
		}
		catch (ObjectOptimisticLockingFailureException ex) {
			throw new ConflictException("Optimistic lock conflict", ex);
		}
	}

	private static Set<String> normalizeTags(Set<String> tags) {
		if (tags == null) {
			return Set.of();
		}
		return new LinkedHashSet<>(tags);
	}

	private static void validateTags(Set<String> tags) {
		for (String tag : tags) {
			if (!AllowedPokemonTags.isAllowed(tag)) {
				throw new InvalidTagException(tag);
			}
		}
	}
}
