package com.sauldaniel.pokemon.application.service;

import com.sauldaniel.pokemon.domain.exception.InvalidPaginationException;
import com.sauldaniel.pokemon.domain.model.PokemonCard;
import com.sauldaniel.pokemon.domain.model.PokemonCardPage;
import com.sauldaniel.pokemon.domain.model.PokemonNamePage;
import com.sauldaniel.pokemon.domain.port.out.PokeApiClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListPokemonCardsServiceTest {

	@Mock
	private PokeApiClient pokeApiClient;

	private ListPokemonCardsService service;

	@BeforeEach
	void setUp() {
		service = new ListPokemonCardsService(pokeApiClient);
	}

	@Test
	void listsPaginatedCardsEnrichedFromPokeApi() {
		when(pokeApiClient.listPokemonNames(0, 2))
				.thenReturn(new PokemonNamePage(List.of("bulbasaur", "ivysaur"), 1302));
		when(pokeApiClient.getPokemonCard("bulbasaur"))
				.thenReturn(new PokemonCard(
						"bulbasaur",
						"https://img/bulbasaur.png",
						"Seed Pokémon",
						6.9,
						List.of("overgrow", "chlorophyll")));
		when(pokeApiClient.getPokemonCard("ivysaur"))
				.thenReturn(new PokemonCard(
						"ivysaur",
						"https://img/ivysaur.png",
						"Seed Pokémon",
						13.0,
						List.of("overgrow", "chlorophyll")));

		PokemonCardPage page = service.execute(0, 2);

		assertThat(page.page()).isZero();
		assertThat(page.size()).isEqualTo(2);
		assertThat(page.totalElements()).isEqualTo(1302);
		assertThat(page.totalPages()).isEqualTo(651);
		assertThat(page.content()).hasSize(2);
		assertThat(page.content().getFirst().name()).isEqualTo("bulbasaur");
		assertThat(page.content().getFirst().spriteUrl()).isEqualTo("https://img/bulbasaur.png");
		assertThat(page.content().getFirst().category()).isEqualTo("Seed Pokémon");
		assertThat(page.content().getFirst().weightKg()).isEqualTo(6.9);
		assertThat(page.content().getFirst().abilities()).containsExactly("overgrow", "chlorophyll");
		assertThat(page.content().get(1).name()).isEqualTo("ivysaur");

		verify(pokeApiClient).listPokemonNames(0, 2);
		verify(pokeApiClient).getPokemonCard("bulbasaur");
		verify(pokeApiClient).getPokemonCard("ivysaur");
	}

	@Test
	void computesOffsetFromPageAndSize() {
		when(pokeApiClient.listPokemonNames(20, 10))
				.thenReturn(new PokemonNamePage(List.of(), 0));

		service.execute(2, 10);

		verify(pokeApiClient).listPokemonNames(20, 10);
	}

	@Test
	void rejectsNegativePage() {
		assertThatThrownBy(() -> service.execute(-1, 10))
				.isInstanceOf(InvalidPaginationException.class)
				.hasMessageContaining("page");
		verifyNoInteractions(pokeApiClient);
	}

	@Test
	void rejectsSizeBelowOne() {
		assertThatThrownBy(() -> service.execute(0, 0))
				.isInstanceOf(InvalidPaginationException.class)
				.hasMessageContaining("size");
		verifyNoInteractions(pokeApiClient);
	}

	@Test
	void rejectsSizeAboveFifty() {
		assertThatThrownBy(() -> service.execute(0, 51))
				.isInstanceOf(InvalidPaginationException.class)
				.hasMessageContaining("size");
		verifyNoInteractions(pokeApiClient);
	}
}
