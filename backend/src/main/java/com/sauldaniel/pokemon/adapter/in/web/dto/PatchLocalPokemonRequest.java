package com.sauldaniel.pokemon.adapter.in.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record PatchLocalPokemonRequest(
		@Size(max = 100) String localizedName,
		@Size(max = 100) String region,
		@Size(max = 2000) String internalNotes,
		Set<@Size(max = 64) String> tags,
		@NotNull Long version
) {
}
