package com.sauldaniel.pokemon.application.service;

import com.sauldaniel.pokemon.domain.model.PokemonDetail;
import com.sauldaniel.pokemon.domain.port.out.PokeApiClient;
import org.springframework.stereotype.Service;

@Service
public class GetPokemonDetailService {

	private final PokeApiClient pokeApiClient;

	public GetPokemonDetailService(PokeApiClient pokeApiClient) {
		this.pokeApiClient = pokeApiClient;
	}

	public PokemonDetail execute(String idOrName) {
		return pokeApiClient.getPokemonDetail(idOrName);
	}
}
