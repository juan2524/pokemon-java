package com.sauldaniel.pokemon.application.service;

import com.sauldaniel.pokemon.domain.exception.ConflictException;
import com.sauldaniel.pokemon.domain.model.LocalPokemon;
import com.sauldaniel.pokemon.domain.model.PokemonCard;
import com.sauldaniel.pokemon.domain.port.out.PokeApiClient;
import com.sauldaniel.pokemon.domain.port.out.PokemonRecordRepositoryPort;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Set;

@Service
public class SyncPokemonService {

	private final PokeApiClient pokeApiClient;
	private final PokemonRecordRepositoryPort repository;

	public SyncPokemonService(PokeApiClient pokeApiClient, PokemonRecordRepositoryPort repository) {
		this.pokeApiClient = pokeApiClient;
		this.repository = repository;
	}

	@Transactional
	public LocalPokemon execute(int pokeApiId) {
		PokemonCard card = pokeApiClient.getPokemonCard(String.valueOf(pokeApiId));
		Instant now = Instant.now();

		LocalPokemon toSave = repository.findByPokeApiId(pokeApiId)
				.map(existing -> new LocalPokemon(
						existing.id(),
						existing.pokeApiId(),
						card.name(),
						existing.localizedName(),
						existing.region(),
						existing.internalNotes(),
						existing.tags(),
						now,
						existing.version()))
				.orElseGet(() -> new LocalPokemon(
						null,
						pokeApiId,
						card.name(),
						null,
						null,
						null,
						Set.of(),
						now,
						0L));

		try {
			return repository.save(toSave);
		}
		catch (DataIntegrityViolationException ex) {
			throw new ConflictException("Duplicate pokeApiId: " + pokeApiId, ex);
		}
	}
}
