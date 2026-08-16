package com.sauldaniel.pokemon.domain.exception;

public class LocalPokemonNotFoundException extends RuntimeException {

	public LocalPokemonNotFoundException(String id) {
		super("Local Pokémon not found: " + id);
	}
}
