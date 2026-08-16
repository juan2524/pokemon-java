package com.sauldaniel.pokemon.application.service;

import com.sauldaniel.pokemon.domain.model.LocalPokemon;
import com.sauldaniel.pokemon.domain.model.PokemonCard;
import com.sauldaniel.pokemon.domain.port.out.PokeApiClient;
import com.sauldaniel.pokemon.domain.port.out.PokemonRecordRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SyncPokemonServiceTest {

	@Mock
	private PokeApiClient pokeApiClient;

	@Mock
	private PokemonRecordRepositoryPort repository;

	private SyncPokemonService service;

	@BeforeEach
	void setUp() {
		service = new SyncPokemonService(pokeApiClient, repository);
	}

	@Test
	void createsLocalRecordFromPokeApi() {
		when(pokeApiClient.getPokemonCard("25")).thenReturn(new PokemonCard(
				"pikachu",
				"https://img/pika.png",
				"Mouse Pokémon",
				6.0,
				List.of("static")));
		when(repository.findByPokeApiId(25)).thenReturn(Optional.empty());
		when(repository.save(any())).thenAnswer(invocation -> {
			LocalPokemon incoming = invocation.getArgument(0);
			return new LocalPokemon(
					UUID.randomUUID(),
					incoming.pokeApiId(),
					incoming.name(),
					incoming.localizedName(),
					incoming.region(),
					incoming.internalNotes(),
					incoming.tags(),
					incoming.syncedAt(),
					incoming.version());
		});

		LocalPokemon saved = service.execute(25);

		assertThat(saved.pokeApiId()).isEqualTo(25);
		assertThat(saved.name()).isEqualTo("pikachu");
		assertThat(saved.localizedName()).isNull();
		assertThat(saved.tags()).isEmpty();

		ArgumentCaptor<LocalPokemon> captor = ArgumentCaptor.forClass(LocalPokemon.class);
		verify(repository).save(captor.capture());
		assertThat(captor.getValue().name()).isEqualTo("pikachu");
		assertThat(captor.getValue().pokeApiId()).isEqualTo(25);
	}

	@Test
	void updatesNameAndSyncedAtWithoutClearingProprietaryFields() {
		UUID id = UUID.randomUUID();
		Instant previousSync = Instant.parse("2024-01-01T00:00:00Z");
		LocalPokemon existing = new LocalPokemon(
				id,
				25,
				"old-name",
				"Pika",
				"Kanto",
				"notes",
				Set.of("favorite"),
				previousSync,
				3L);
		when(pokeApiClient.getPokemonCard("25")).thenReturn(new PokemonCard(
				"pikachu", "u", "c", 6.0, List.of()));
		when(repository.findByPokeApiId(25)).thenReturn(Optional.of(existing));
		when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		LocalPokemon saved = service.execute(25);

		assertThat(saved.id()).isEqualTo(id);
		assertThat(saved.name()).isEqualTo("pikachu");
		assertThat(saved.localizedName()).isEqualTo("Pika");
		assertThat(saved.region()).isEqualTo("Kanto");
		assertThat(saved.internalNotes()).isEqualTo("notes");
		assertThat(saved.tags()).containsExactly("favorite");
		assertThat(saved.version()).isEqualTo(3L);
		assertThat(saved.syncedAt()).isAfter(previousSync);
	}
}
