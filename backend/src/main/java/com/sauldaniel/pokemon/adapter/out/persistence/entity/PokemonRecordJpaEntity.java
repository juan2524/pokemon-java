package com.sauldaniel.pokemon.adapter.out.persistence.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "pokemon_record")
public class PokemonRecordJpaEntity {

	@Id
	private UUID id;

	@Column(name = "poke_api_id", nullable = false, unique = true)
	private int pokeApiId;

	@Column(nullable = false, length = 100)
	private String name;

	@Column(name = "localized_name", length = 100)
	private String localizedName;

	@Column(length = 100)
	private String region;

	@Column(name = "internal_notes", length = 2000)
	private String internalNotes;

	@Column(name = "synced_at", nullable = false)
	private Instant syncedAt;

	@Version
	@Column(nullable = false)
	private long version;

	@OneToMany(mappedBy = "pokemon", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	private Set<PokemonTagJpaEntity> tags = new HashSet<>();

	protected PokemonRecordJpaEntity() {
	}

	public PokemonRecordJpaEntity(
			UUID id,
			int pokeApiId,
			String name,
			String localizedName,
			String region,
			String internalNotes,
			Instant syncedAt,
			long version) {
		this.id = id;
		this.pokeApiId = pokeApiId;
		this.name = name;
		this.localizedName = localizedName;
		this.region = region;
		this.internalNotes = internalNotes;
		this.syncedAt = syncedAt;
		this.version = version;
	}

	public UUID getId() {
		return id;
	}

	public int getPokeApiId() {
		return pokeApiId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLocalizedName() {
		return localizedName;
	}

	public void setLocalizedName(String localizedName) {
		this.localizedName = localizedName;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public String getInternalNotes() {
		return internalNotes;
	}

	public void setInternalNotes(String internalNotes) {
		this.internalNotes = internalNotes;
	}

	public Instant getSyncedAt() {
		return syncedAt;
	}

	public void setSyncedAt(Instant syncedAt) {
		this.syncedAt = syncedAt;
	}

	public long getVersion() {
		return version;
	}

	public void setVersion(long version) {
		this.version = version;
	}

	public Set<PokemonTagJpaEntity> getTags() {
		return tags;
	}

	public void replaceTags(Set<String> tagValues) {
		tags.clear();
		if (tagValues == null) {
			return;
		}
		for (String tag : tagValues) {
			tags.add(new PokemonTagJpaEntity(UUID.randomUUID(), this, tag));
		}
	}
}
