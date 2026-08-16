package com.sauldaniel.pokemon.application.service;

import com.sauldaniel.pokemon.domain.exception.LocalPokemonNotFoundException;
import com.sauldaniel.pokemon.domain.port.out.PokemonRecordRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class DeleteLocalPokemonService {

	private final PokemonRecordRepositoryPort repository;

	public DeleteLocalPokemonService(PokemonRecordRepositoryPort repository) {
		this.repository = repository;
	}

	@Transactional
	public void execute(UUID id) {
		if (repository.findById(id).isEmpty()) {
			throw new LocalPokemonNotFoundException(id.toString());
		}
		repository.deleteById(id);
	}
}
