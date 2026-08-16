package com.sauldaniel.pokemon.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "pokemon_tag")
public class PokemonTagJpaEntity {

	@Id
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "pokemon_id", nullable = false)
	private PokemonRecordJpaEntity pokemon;

	@Column(nullable = false, length = 64)
	private String tag;

	protected PokemonTagJpaEntity() {
	}

	public PokemonTagJpaEntity(UUID id, PokemonRecordJpaEntity pokemon, String tag) {
		this.id = id;
		this.pokemon = pokemon;
		this.tag = tag;
	}

	public UUID getId() {
		return id;
	}

	public PokemonRecordJpaEntity getPokemon() {
		return pokemon;
	}

	public String getTag() {
		return tag;
	}
}
