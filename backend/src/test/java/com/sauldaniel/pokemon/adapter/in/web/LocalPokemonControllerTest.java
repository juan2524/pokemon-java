package com.sauldaniel.pokemon.adapter.in.web;

import com.sauldaniel.pokemon.application.service.DeleteLocalPokemonService;
import com.sauldaniel.pokemon.application.service.GetLocalPokemonService;
import com.sauldaniel.pokemon.application.service.ListLocalPokemonService;
import com.sauldaniel.pokemon.application.service.UpdateLocalPokemonCommand;
import com.sauldaniel.pokemon.application.service.UpdateLocalPokemonService;
import com.sauldaniel.pokemon.domain.exception.ConflictException;
import com.sauldaniel.pokemon.domain.exception.InvalidTagException;
import com.sauldaniel.pokemon.domain.exception.LocalPokemonNotFoundException;
import com.sauldaniel.pokemon.domain.model.LocalPokemon;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = LocalPokemonController.class)
@Import({
		GlobalExceptionHandler.class,
		com.sauldaniel.pokemon.config.SecurityConfig.class,
		com.sauldaniel.pokemon.config.JwtConfig.class
})
@TestPropertySource(properties = "app.security.jwt-secret=test-secret-key-at-least-32-chars!!")
@WithMockUser(roles = "USER")
class LocalPokemonControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private ListLocalPokemonService listLocalPokemonService;

	@MockitoBean
	private GetLocalPokemonService getLocalPokemonService;

	@MockitoBean
	private UpdateLocalPokemonService updateLocalPokemonService;

	@MockitoBean
	private DeleteLocalPokemonService deleteLocalPokemonService;

	@Test
	void listsLocalPokemon() throws Exception {
		UUID id = UUID.randomUUID();
		when(listLocalPokemonService.execute()).thenReturn(List.of(sample(id)));

		mockMvc.perform(get("/api/v1/local/pokemon"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value(id.toString()))
				.andExpect(jsonPath("$[0].pokeApiId").value(25))
				.andExpect(jsonPath("$[0].name").value("pikachu"))
				.andExpect(jsonPath("$[0].localizedName").value("Pika"))
				.andExpect(jsonPath("$[0].tags[0]").value("favorite"));
	}

	@Test
	void getsLocalPokemonById() throws Exception {
		UUID id = UUID.randomUUID();
		when(getLocalPokemonService.execute(id)).thenReturn(sample(id));

		mockMvc.perform(get("/api/v1/local/pokemon/{id}", id))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(id.toString()))
				.andExpect(jsonPath("$.version").value(2));
	}

	@Test
	void returns404WhenLocalPokemonMissing() throws Exception {
		UUID id = UUID.randomUUID();
		when(getLocalPokemonService.execute(id)).thenThrow(new LocalPokemonNotFoundException(id.toString()));

		mockMvc.perform(get("/api/v1/local/pokemon/{id}", id))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404));
	}

	@Test
	void patchesProprietaryFields() throws Exception {
		UUID id = UUID.randomUUID();
		when(updateLocalPokemonService.execute(eq(id), any())).thenReturn(sample(id));

		mockMvc.perform(patch("/api/v1/local/pokemon/{id}", id)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "localizedName": "Pika",
								  "region": "Kanto",
								  "internalNotes": "notes",
								  "tags": ["favorite"],
								  "version": 2
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.localizedName").value("Pika"));

		ArgumentCaptor<UpdateLocalPokemonCommand> captor = ArgumentCaptor.forClass(UpdateLocalPokemonCommand.class);
		verify(updateLocalPokemonService).execute(eq(id), captor.capture());
		assertThat(captor.getValue().version()).isEqualTo(2L);
		assertThat(captor.getValue().tags()).containsExactly("favorite");
	}

	@Test
	void rejectsOversizedPatchFields() throws Exception {
		UUID id = UUID.randomUUID();
		String tooLong = "x".repeat(101);

		mockMvc.perform(patch("/api/v1/local/pokemon/{id}", id)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "localizedName": "%s",
								  "region": null,
								  "internalNotes": null,
								  "tags": [],
								  "version": 0
								}
								""".formatted(tooLong)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.errors").isArray());
	}

	@Test
	void returns400ForUnknownTags() throws Exception {
		UUID id = UUID.randomUUID();
		when(updateLocalPokemonService.execute(eq(id), any()))
				.thenThrow(new InvalidTagException("bogus"));

		mockMvc.perform(patch("/api/v1/local/pokemon/{id}", id)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"localizedName":null,"region":null,"internalNotes":null,"tags":["bogus"],"version":0}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.detail").value("Unknown tag: bogus"));
	}

	@Test
	void returns409OnOptimisticLockConflict() throws Exception {
		UUID id = UUID.randomUUID();
		when(updateLocalPokemonService.execute(eq(id), any()))
				.thenThrow(new ConflictException("Optimistic lock conflict"));

		mockMvc.perform(patch("/api/v1/local/pokemon/{id}", id)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"localizedName":"Pika","region":null,"internalNotes":null,"tags":[],"version":1}
								"""))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.status").value(409));
	}

	@Test
	void rejectsUnauthenticatedAccessToLocalRoster() throws Exception {
		mockMvc.perform(get("/api/v1/local/pokemon").with(anonymous()))
				.andExpect(status().isUnauthorized());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void deletesLocalPokemonWhenCallerIsAdmin() throws Exception {
		UUID id = UUID.randomUUID();

		mockMvc.perform(delete("/api/v1/local/pokemon/{id}", id))
				.andExpect(status().isNoContent());

		verify(deleteLocalPokemonService).execute(id);
	}

	@Test
	void rejectsUserRoleWhenDeletingLocalPokemon() throws Exception {
		UUID id = UUID.randomUUID();

		mockMvc.perform(delete("/api/v1/local/pokemon/{id}", id))
				.andExpect(status().isForbidden());
	}

	private static LocalPokemon sample(UUID id) {
		return new LocalPokemon(
				id,
				25,
				"pikachu",
				"Pika",
				"Kanto",
				"notes",
				Set.of("favorite"),
				Instant.parse("2024-06-01T00:00:00Z"),
				2L);
	}
}
