package com.sauldaniel.pokemon.application.service;

import com.sauldaniel.pokemon.domain.exception.InvalidPaginationException;
import com.sauldaniel.pokemon.domain.model.PokemonCard;
import com.sauldaniel.pokemon.domain.model.PokemonCardPage;
import com.sauldaniel.pokemon.domain.model.PokemonNamePage;
import com.sauldaniel.pokemon.domain.port.out.PokeApiClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ListPokemonCardsService {

	private final PokeApiClient pokeApiClient;

	public ListPokemonCardsService(PokeApiClient pokeApiClient) {
		this.pokeApiClient = pokeApiClient;
	}

	public PokemonCardPage execute(int page, int size) {
		validate(page, size);

		int offset = page * size;
		PokemonNamePage namePage = pokeApiClient.listPokemonNames(offset, size);

		List<PokemonCard> cards = new ArrayList<>(namePage.names().size());
		for (String name : namePage.names()) {
			cards.add(pokeApiClient.getPokemonCard(name));
		}

		int totalPages = namePage.totalCount() == 0
				? 0
				: (int) Math.ceil((double) namePage.totalCount() / size);

		return new PokemonCardPage(cards, page, size, namePage.totalCount(), totalPages);
	}

	private static void validate(int page, int size) {
		if (page < 0) {
			throw new InvalidPaginationException("page must be greater than or equal to 0");
		}
		if (size < 1 || size > 50) {
			throw new InvalidPaginationException("size must be between 1 and 50");
		}
	}
}
