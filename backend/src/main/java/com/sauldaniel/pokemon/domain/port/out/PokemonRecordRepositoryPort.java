package com.sauldaniel.pokemon.domain.port.out;

import com.sauldaniel.pokemon.domain.model.LocalPokemon;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PokemonRecordRepositoryPort {

	LocalPokemon save(LocalPokemon pokemon);

	Optional<LocalPokemon> findById(UUID id);

	Optional<LocalPokemon> findByPokeApiId(int pokeApiId);

	List<LocalPokemon> findAll();

	void deleteById(UUID id);

	boolean existsByPokeApiId(int pokeApiId);
}
