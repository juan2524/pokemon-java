package com.sauldaniel.pokemon.domain.exception;

public class PokemonNotFoundException extends RuntimeException {

	public PokemonNotFoundException(String idOrName) {
		super("Pokémon not found: " + idOrName);
	}
}
