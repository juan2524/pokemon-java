package com.sauldaniel.pokemon.adapter.in.web;

import com.sauldaniel.pokemon.application.service.SyncPokemonService;
import com.sauldaniel.pokemon.domain.exception.ConflictException;
import com.sauldaniel.pokemon.domain.exception.PokemonNotFoundException;
import com.sauldaniel.pokemon.domain.model.LocalPokemon;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PokemonSyncController.class)
@Import({
		GlobalExceptionHandler.class,
		com.sauldaniel.pokemon.config.SecurityConfig.class,
		com.sauldaniel.pokemon.config.JwtConfig.class
})
@TestPropertySource(properties = "app.security.jwt-secret=test-secret-key-at-least-32-chars!!")
@WithMockUser(roles = "ADMIN")
class PokemonSyncControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private SyncPokemonService syncPokemonService;

	@Test
	void syncsPokemonByPokeApiId() throws Exception {
		UUID id = UUID.randomUUID();
		when(syncPokemonService.execute(25)).thenReturn(new LocalPokemon(
				id,
				25,
				"pikachu",
				null,
				null,
				null,
				Set.of(),
				Instant.parse("2024-06-01T00:00:00Z"),
				0L));

		mockMvc.perform(post("/api/v1/pokemon/{pokeApiId}/sync", 25))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(id.toString()))
				.andExpect(jsonPath("$.pokeApiId").value(25))
				.andExpect(jsonPath("$.name").value("pikachu"));
	}

	@Test
	void returns404WhenUpstreamMissing() throws Exception {
		when(syncPokemonService.execute(99999)).thenThrow(new PokemonNotFoundException("99999"));

		mockMvc.perform(post("/api/v1/pokemon/{pokeApiId}/sync", 99999))
				.andExpect(status().isNotFound());
	}

	@Test
	void returns409OnDuplicatePokeApiId() throws Exception {
		when(syncPokemonService.execute(25)).thenThrow(new ConflictException("Duplicate pokeApiId: 25"));

		mockMvc.perform(post("/api/v1/pokemon/{pokeApiId}/sync", 25))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.status").value(409));
	}

	@Test
	@WithMockUser(roles = "USER")
	void rejectsUserRoleWhenSyncingPokemon() throws Exception {
		mockMvc.perform(post("/api/v1/pokemon/{pokeApiId}/sync", 25))
				.andExpect(status().isForbidden());
	}
}
