package com.sauldaniel.pokemon.domain.port.out;

import com.sauldaniel.pokemon.domain.model.UserAccount;

import java.util.Optional;
import java.util.UUID;

public interface UserRepositoryPort {

	UserAccount save(UserAccount user);

	Optional<UserAccount> findById(UUID id);

	Optional<UserAccount> findByEmail(String email);

	boolean existsByEmail(String email);
}
