package com.sauldaniel.pokemon.adapter.out.persistence;

import com.sauldaniel.pokemon.adapter.out.persistence.entity.PokemonRecordJpaEntity;
import com.sauldaniel.pokemon.adapter.out.persistence.repo.SpringDataPokemonRecordRepository;
import com.sauldaniel.pokemon.domain.model.LocalPokemon;
import com.sauldaniel.pokemon.domain.port.out.PokemonRecordRepositoryPort;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@Transactional
public class PokemonRecordRepositoryAdapter implements PokemonRecordRepositoryPort {

	private final SpringDataPokemonRecordRepository repository;

	public PokemonRecordRepositoryAdapter(SpringDataPokemonRecordRepository repository) {
		this.repository = repository;
	}

	@Override
	public LocalPokemon save(LocalPokemon pokemon) {
		PokemonRecordJpaEntity entity;
		if (pokemon.id() == null) {
			entity = new PokemonRecordJpaEntity(
					UUID.randomUUID(),
					pokemon.pokeApiId(),
					pokemon.name(),
					pokemon.localizedName(),
					pokemon.region(),
					pokemon.internalNotes(),
					pokemon.syncedAt(),
					0L);
			entity.replaceTags(pokemon.tags());
		}
		else {
			entity = repository.findById(pokemon.id())
					.orElseThrow(() -> new ObjectOptimisticLockingFailureException(
							PokemonRecordJpaEntity.class,
							pokemon.id()));
			if (entity.getVersion() != pokemon.version()) {
				throw new ObjectOptimisticLockingFailureException(PokemonRecordJpaEntity.class, pokemon.id());
			}
			entity.setName(pokemon.name());
			entity.setLocalizedName(pokemon.localizedName());
			entity.setRegion(pokemon.region());
			entity.setInternalNotes(pokemon.internalNotes());
			entity.setSyncedAt(pokemon.syncedAt());
			entity.replaceTags(pokemon.tags());
		}
		return toDomain(repository.saveAndFlush(entity));
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<LocalPokemon> findById(UUID id) {
		return repository.findById(id).map(PokemonRecordRepositoryAdapter::toDomain);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<LocalPokemon> findByPokeApiId(int pokeApiId) {
		return repository.findByPokeApiId(pokeApiId).map(PokemonRecordRepositoryAdapter::toDomain);
	}

	@Override
	@Transactional(readOnly = true)
	public List<LocalPokemon> findAll() {
		return repository.findAll().stream().map(PokemonRecordRepositoryAdapter::toDomain).toList();
	}

	@Override
	public void deleteById(UUID id) {
		repository.deleteById(id);
	}

	@Override
	@Transactional(readOnly = true)
	public boolean existsByPokeApiId(int pokeApiId) {
		return repository.existsByPokeApiId(pokeApiId);
	}

	private static LocalPokemon toDomain(PokemonRecordJpaEntity entity) {
		Set<String> tags = entity.getTags().stream()
				.map(tag -> tag.getTag())
				.collect(Collectors.toSet());
		return new LocalPokemon(
				entity.getId(),
				entity.getPokeApiId(),
				entity.getName(),
				entity.getLocalizedName(),
				entity.getRegion(),
				entity.getInternalNotes(),
				tags,
				entity.getSyncedAt(),
				entity.getVersion());
	}
}
