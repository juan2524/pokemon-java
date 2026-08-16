package com.sauldaniel.pokemon.adapter.out.persistence.repo;

import com.sauldaniel.pokemon.adapter.out.persistence.entity.PokemonRecordJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataPokemonRecordRepository extends JpaRepository<PokemonRecordJpaEntity, UUID> {

	Optional<PokemonRecordJpaEntity> findByPokeApiId(int pokeApiId);

	boolean existsByPokeApiId(int pokeApiId);
}
