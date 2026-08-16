package com.sauldaniel.pokemon.adapter.out.persistence;

import com.sauldaniel.pokemon.domain.model.LocalPokemon;
import com.sauldaniel.pokemon.domain.model.Role;
import com.sauldaniel.pokemon.domain.model.UserAccount;
import com.sauldaniel.pokemon.domain.port.out.PokemonRecordRepositoryPort;
import com.sauldaniel.pokemon.domain.port.out.UserRepositoryPort;
import com.sauldaniel.pokemon.support.PostgresTestcontainersSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class PokemonRecordRepositoryAdapterTest extends PostgresTestcontainersSupport {

	@Autowired
	private PokemonRecordRepositoryPort pokemonRecords;

	@Autowired
	private UserRepositoryPort users;

	@Test
	void savesAndLoadsPokemonWithTags() {
		LocalPokemon saved = pokemonRecords.save(new LocalPokemon(
				null,
				25,
				"pikachu",
				"Pika",
				"Kanto",
				"notes",
				Set.of("favorite", "team"),
				Instant.parse("2024-06-01T00:00:00Z"),
				0L));

		assertThat(saved.id()).isNotNull();
		LocalPokemon loaded = pokemonRecords.findById(saved.id()).orElseThrow();
		assertThat(loaded.pokeApiId()).isEqualTo(25);
		assertThat(loaded.tags()).containsExactlyInAnyOrder("favorite", "team");
		assertThat(pokemonRecords.findByPokeApiId(25)).isPresent();
	}

	@Test
	void enforcesUniquePokeApiId() {
		pokemonRecords.save(new LocalPokemon(
				null, 1, "bulbasaur", null, null, null, Set.of(), Instant.now(), 0L));

		assertThatThrownBy(() -> pokemonRecords.save(new LocalPokemon(
				null, 1, "duplicate", null, null, null, Set.of(), Instant.now(), 0L)))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void optimisticLockRejectsStaleVersion() {
		LocalPokemon saved = pokemonRecords.save(new LocalPokemon(
				null, 4, "charmander", null, null, null, Set.of(), Instant.now(), 0L));

		LocalPokemon first = pokemonRecords.findById(saved.id()).orElseThrow();
		LocalPokemon second = pokemonRecords.findById(saved.id()).orElseThrow();

		pokemonRecords.save(new LocalPokemon(
				first.id(),
				first.pokeApiId(),
				first.name(),
				"Charmy",
				first.region(),
				first.internalNotes(),
				first.tags(),
				first.syncedAt(),
				first.version()));

		assertThatThrownBy(() -> pokemonRecords.save(new LocalPokemon(
				second.id(),
				second.pokeApiId(),
				second.name(),
				"Stale",
				second.region(),
				second.internalNotes(),
				second.tags(),
				second.syncedAt(),
				second.version())))
				.isInstanceOf(ObjectOptimisticLockingFailureException.class);
	}

	@Test
	void savesUserByEmail() {
		UserAccount saved = users.save(new UserAccount(
				null,
				"unique-user@example.com",
				"$2a$10$hash",
				Role.USER,
				Instant.parse("2024-01-01T00:00:00Z")));

		assertThat(saved.id()).isNotNull();
		assertThat(users.findByEmail("unique-user@example.com")).isPresent();
		assertThat(users.existsByEmail("unique-user@example.com")).isTrue();
	}

	@Test
	void deletesPokemonAndCascadesTags() {
		LocalPokemon saved = pokemonRecords.save(new LocalPokemon(
				null, 7, "squirtle", null, null, null, Set.of("starter"), Instant.now(), 0L));
		UUID id = saved.id();

		pokemonRecords.deleteById(id);

		assertThat(pokemonRecords.findById(id)).isEmpty();
	}
}
