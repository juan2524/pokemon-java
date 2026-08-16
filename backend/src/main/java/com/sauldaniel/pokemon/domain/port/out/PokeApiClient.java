package com.sauldaniel.pokemon.domain.port.out;

import com.sauldaniel.pokemon.domain.model.PokemonCard;
import com.sauldaniel.pokemon.domain.model.PokemonDetail;
import com.sauldaniel.pokemon.domain.model.PokemonNamePage;

public interface PokeApiClient {

	PokemonNamePage listPokemonNames(int offset, int limit);

	PokemonCard getPokemonCard(String name);

	PokemonDetail getPokemonDetail(String idOrName);
}
