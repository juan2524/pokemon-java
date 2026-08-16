package com.sauldaniel.pokemon.adapter.out.persistence.repo;

import com.sauldaniel.pokemon.adapter.out.persistence.entity.UserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataUserRepository extends JpaRepository<UserJpaEntity, UUID> {

	Optional<UserJpaEntity> findByEmail(String email);

	boolean existsByEmail(String email);
}
