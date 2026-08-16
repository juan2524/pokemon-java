package com.sauldaniel.pokemon.application.service;

import com.sauldaniel.pokemon.domain.exception.PokemonNotFoundException;
import com.sauldaniel.pokemon.domain.model.EvolutionNode;
import com.sauldaniel.pokemon.domain.model.PokemonDetail;
import com.sauldaniel.pokemon.domain.model.PokemonStat;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetPokemonDetailServiceTest {

	@Mock
	private PokeApiClient pokeApiClient;

	private GetPokemonDetailService service;

	@BeforeEach
	void setUp() {
		service = new GetPokemonDetailService(pokeApiClient);
	}

	@Test
	void returnsDetailFromPokeApi() {
		PokemonDetail detail = new PokemonDetail(
				"bulbasaur",
				"https://img/official.png",
				0.7,
				6.9,
				List.of("grass", "poison"),
				List.of(
						new PokemonStat("hp", 45),
						new PokemonStat("attack", 49),
						new PokemonStat("defense", 49),
						new PokemonStat("special-attack", 65),
						new PokemonStat("special-defense", 65),
						new PokemonStat("speed", 45)),
				"A strange seed was planted on its back at birth.",
				List.of(
						new EvolutionNode("bulbasaur"),
						new EvolutionNode("ivysaur"),
						new EvolutionNode("venusaur")));
		when(pokeApiClient.getPokemonDetail("bulbasaur")).thenReturn(detail);

		PokemonDetail result = service.execute("bulbasaur");

		assertThat(result).isEqualTo(detail);
		verify(pokeApiClient).getPokemonDetail("bulbasaur");
	}

	@Test
	void propagatesNotFound() {
		when(pokeApiClient.getPokemonDetail("missingno"))
				.thenThrow(new PokemonNotFoundException("missingno"));

		assertThatThrownBy(() -> service.execute("missingno"))
				.isInstanceOf(PokemonNotFoundException.class)
				.hasMessageContaining("missingno");
	}
}
