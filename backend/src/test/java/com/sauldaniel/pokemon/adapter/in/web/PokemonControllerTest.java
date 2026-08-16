package com.sauldaniel.pokemon.adapter.in.web;

import com.sauldaniel.pokemon.application.service.GetPokemonDetailService;
import com.sauldaniel.pokemon.application.service.ListPokemonCardsService;
import com.sauldaniel.pokemon.domain.exception.PokemonNotFoundException;
import com.sauldaniel.pokemon.domain.model.EvolutionNode;
import com.sauldaniel.pokemon.domain.model.PokemonCard;
import com.sauldaniel.pokemon.domain.model.PokemonCardPage;
import com.sauldaniel.pokemon.domain.model.PokemonDetail;
import com.sauldaniel.pokemon.domain.model.PokemonStat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PokemonController.class)
@Import({
		GlobalExceptionHandler.class,
		com.sauldaniel.pokemon.config.SecurityConfig.class,
		com.sauldaniel.pokemon.config.JwtConfig.class
})
@TestPropertySource(properties = "app.security.jwt-secret=test-secret-key-at-least-32-chars!!")
class PokemonControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private ListPokemonCardsService listPokemonCardsService;

	@MockitoBean
	private GetPokemonDetailService getPokemonDetailService;

	@Test
	void returnsPokemonDetail() throws Exception {
		when(getPokemonDetailService.execute("1")).thenReturn(new PokemonDetail(
				"bulbasaur",
				"https://img/official.png",
				0.7,
				6.9,
				List.of("grass", "poison"),
				List.of(new PokemonStat("hp", 45), new PokemonStat("attack", 49)),
				"A strange seed was planted on its back at birth.",
				List.of(
						new EvolutionNode("bulbasaur"),
						new EvolutionNode("ivysaur"),
						new EvolutionNode("venusaur"))));

		mockMvc.perform(get("/api/v1/pokemon/{idOrName}", "1")
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("bulbasaur"))
				.andExpect(jsonPath("$.imageUrl").value("https://img/official.png"))
				.andExpect(jsonPath("$.heightM").value(0.7))
				.andExpect(jsonPath("$.weightKg").value(6.9))
				.andExpect(jsonPath("$.types[0]").value("grass"))
				.andExpect(jsonPath("$.types[1]").value("poison"))
				.andExpect(jsonPath("$.stats[0].name").value("hp"))
				.andExpect(jsonPath("$.stats[0].baseValue").value(45))
				.andExpect(jsonPath("$.flavorTextEn").value("A strange seed was planted on its back at birth."))
				.andExpect(jsonPath("$.evolutionLineage[0].name").value("bulbasaur"))
				.andExpect(jsonPath("$.evolutionLineage[2].name").value("venusaur"));
	}

	@Test
	void returnsNotFoundWhenPokemonMissing() throws Exception {
		when(getPokemonDetailService.execute("missingno"))
				.thenThrow(new PokemonNotFoundException("missingno"));

		mockMvc.perform(get("/api/v1/pokemon/{idOrName}", "missingno"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.title").value("Not Found"))
				.andExpect(jsonPath("$.detail").value("Pokémon not found: missingno"));
	}

	@Test
	void returnsPaginatedPokemonCards() throws Exception {
		when(listPokemonCardsService.execute(0, 2)).thenReturn(new PokemonCardPage(
				List.of(new PokemonCard(
						"bulbasaur",
						"https://img/bulbasaur.png",
						"Seed Pokémon",
						6.9,
						List.of("overgrow"))),
				0,
				2,
				1302,
				651));

		mockMvc.perform(get("/api/v1/pokemon")
						.param("page", "0")
						.param("size", "2")
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.page").value(0))
				.andExpect(jsonPath("$.size").value(2))
				.andExpect(jsonPath("$.totalElements").value(1302))
				.andExpect(jsonPath("$.totalPages").value(651))
				.andExpect(jsonPath("$.content[0].name").value("bulbasaur"))
				.andExpect(jsonPath("$.content[0].spriteUrl").value("https://img/bulbasaur.png"))
				.andExpect(jsonPath("$.content[0].category").value("Seed Pokémon"))
				.andExpect(jsonPath("$.content[0].weightKg").value(6.9))
				.andExpect(jsonPath("$.content[0].abilities[0]").value("overgrow"));
	}

	@Test
	void rejectsNegativePage() throws Exception {
		mockMvc.perform(get("/api/v1/pokemon")
						.param("page", "-1")
						.param("size", "10"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.title").exists())
				.andExpect(jsonPath("$.errors").isArray());

		verify(listPokemonCardsService, never()).execute(anyInt(), anyInt());
	}

	@Test
	void rejectsSizeAboveFifty() throws Exception {
		mockMvc.perform(get("/api/v1/pokemon")
						.param("page", "0")
						.param("size", "51"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.errors").isArray());

		verify(listPokemonCardsService, never()).execute(anyInt(), anyInt());
	}

	@Test
	void rejectsSizeBelowOne() throws Exception {
		mockMvc.perform(get("/api/v1/pokemon")
						.param("page", "0")
						.param("size", "0"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400));

		verify(listPokemonCardsService, never()).execute(anyInt(), anyInt());
	}
}
