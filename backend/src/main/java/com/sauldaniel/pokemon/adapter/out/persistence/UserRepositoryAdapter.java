package com.sauldaniel.pokemon.adapter.out.persistence;

import com.sauldaniel.pokemon.adapter.out.persistence.entity.UserJpaEntity;
import com.sauldaniel.pokemon.adapter.out.persistence.repo.SpringDataUserRepository;
import com.sauldaniel.pokemon.domain.model.UserAccount;
import com.sauldaniel.pokemon.domain.port.out.UserRepositoryPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Component
@Transactional
public class UserRepositoryAdapter implements UserRepositoryPort {

	private final SpringDataUserRepository repository;

	public UserRepositoryAdapter(SpringDataUserRepository repository) {
		this.repository = repository;
	}

	@Override
	public UserAccount save(UserAccount user) {
		UUID id = user.id() == null ? UUID.randomUUID() : user.id();
		UserJpaEntity entity = new UserJpaEntity(
				id,
				user.email(),
				user.passwordHash(),
				user.role(),
				user.createdAt());
		return toDomain(repository.save(entity));
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<UserAccount> findById(UUID id) {
		return repository.findById(id).map(UserRepositoryAdapter::toDomain);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<UserAccount> findByEmail(String email) {
		return repository.findByEmail(email).map(UserRepositoryAdapter::toDomain);
	}

	@Override
	@Transactional(readOnly = true)
	public boolean existsByEmail(String email) {
		return repository.existsByEmail(email);
	}

	private static UserAccount toDomain(UserJpaEntity entity) {
		return new UserAccount(
				entity.getId(),
				entity.getEmail(),
				entity.getPasswordHash(),
				entity.getRole(),
				entity.getCreatedAt());
	}
}
