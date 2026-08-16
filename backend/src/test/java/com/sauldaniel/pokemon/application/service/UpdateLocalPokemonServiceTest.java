package com.sauldaniel.pokemon.application.service;

import com.sauldaniel.pokemon.domain.exception.InvalidTagException;
import com.sauldaniel.pokemon.domain.exception.LocalPokemonNotFoundException;
import com.sauldaniel.pokemon.domain.model.LocalPokemon;
import com.sauldaniel.pokemon.domain.port.out.PokemonRecordRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateLocalPokemonServiceTest {

	@Mock
	private PokemonRecordRepositoryPort repository;

	private UpdateLocalPokemonService service;

	@BeforeEach
	void setUp() {
		service = new UpdateLocalPokemonService(repository);
	}

	@Test
	void updatesProprietaryFieldsOnly() {
		UUID id = UUID.randomUUID();
		LocalPokemon existing = new LocalPokemon(
				id,
				25,
				"pikachu",
				null,
				null,
				null,
				Set.of(),
				Instant.parse("2024-01-01T00:00:00Z"),
				0L);
		when(repository.findById(id)).thenReturn(Optional.of(existing));
		when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		LocalPokemon updated = service.execute(id, new UpdateLocalPokemonCommand(
				"Pika",
				"Kanto",
				"Team mascot",
				Set.of("favorite", "team"),
				0L));

		assertThat(updated.name()).isEqualTo("pikachu");
		assertThat(updated.pokeApiId()).isEqualTo(25);
		assertThat(updated.localizedName()).isEqualTo("Pika");
		assertThat(updated.region()).isEqualTo("Kanto");
		assertThat(updated.internalNotes()).isEqualTo("Team mascot");
		assertThat(updated.tags()).containsExactlyInAnyOrder("favorite", "team");
		assertThat(updated.syncedAt()).isEqualTo(existing.syncedAt());
		assertThat(updated.version()).isEqualTo(0L);

		ArgumentCaptor<LocalPokemon> captor = ArgumentCaptor.forClass(LocalPokemon.class);
		verify(repository).save(captor.capture());
		assertThat(captor.getValue().name()).isEqualTo("pikachu");
		assertThat(captor.getValue().pokeApiId()).isEqualTo(25);
	}

	@Test
	void rejectsUnknownTags() {
		UUID id = UUID.randomUUID();
		when(repository.findById(id)).thenReturn(Optional.of(new LocalPokemon(
				id, 1, "bulbasaur", null, null, null, Set.of(), Instant.now(), 0L)));

		assertThatThrownBy(() -> service.execute(id, new UpdateLocalPokemonCommand(
				null, null, null, Set.of("not-a-real-tag"), 0L)))
				.isInstanceOf(InvalidTagException.class)
				.hasMessageContaining("not-a-real-tag");
	}

	@Test
	void throwsWhenMissing() {
		UUID id = UUID.randomUUID();
		when(repository.findById(id)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.execute(id, new UpdateLocalPokemonCommand(
				"x", null, null, Set.of(), 0L)))
				.isInstanceOf(LocalPokemonNotFoundException.class);
	}
}
