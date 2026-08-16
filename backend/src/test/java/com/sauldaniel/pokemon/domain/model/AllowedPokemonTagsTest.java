package com.sauldaniel.pokemon.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AllowedPokemonTagsTest {

	@Test
	void acceptsKnownTags() {
		assertThat(AllowedPokemonTags.isAllowed("starter")).isTrue();
		assertThat(AllowedPokemonTags.isAllowed("competitive")).isTrue();
	}

	@Test
	void rejectsUnknownOrBlankTags() {
		assertThat(AllowedPokemonTags.isAllowed("bogus")).isFalse();
		assertThat(AllowedPokemonTags.isAllowed(null)).isFalse();
		assertThat(AllowedPokemonTags.isAllowed("")).isFalse();
	}
}
