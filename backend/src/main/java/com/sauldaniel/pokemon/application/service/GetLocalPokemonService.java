package com.sauldaniel.pokemon.application.service;

import com.sauldaniel.pokemon.domain.exception.LocalPokemonNotFoundException;
import com.sauldaniel.pokemon.domain.model.LocalPokemon;
import com.sauldaniel.pokemon.domain.port.out.PokemonRecordRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class GetLocalPokemonService {

	private final PokemonRecordRepositoryPort repository;

	public GetLocalPokemonService(PokemonRecordRepositoryPort repository) {
		this.repository = repository;
	}

	@Transactional(readOnly = true)
	public LocalPokemon execute(UUID id) {
		return repository.findById(id)
				.orElseThrow(() -> new LocalPokemonNotFoundException(id.toString()));
	}
}
