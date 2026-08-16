package com.sauldaniel.pokemon.adapter.out.pokeapi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sauldaniel.pokemon.domain.exception.PokemonNotFoundException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class PokeApiResourceClient {

	private final RestClient restClient;

	public PokeApiResourceClient(RestClient pokeApiRestClient) {
		this.restClient = pokeApiRestClient;
	}

	@Cacheable(cacheNames = "pokeapi-pokemon", key = "#idOrName")
	public PokemonJson fetchPokemon(String idOrName) {
		return restClient.get()
				.uri("/pokemon/{idOrName}", idOrName)
				.retrieve()
				.onStatus(status -> status.value() == 404, (request, response) -> {
					throw new PokemonNotFoundException(idOrName);
				})
				.body(PokemonJson.class);
	}

	@Cacheable(cacheNames = "pokeapi-species", key = "#idOrName")
	public SpeciesJson fetchSpecies(String idOrName) {
		return restClient.get()
				.uri("/pokemon-species/{idOrName}", idOrName)
				.retrieve()
				.onStatus(status -> status.value() == 404, (request, response) -> {
					throw new PokemonNotFoundException(idOrName);
				})
				.body(SpeciesJson.class);
	}

	@Cacheable(cacheNames = "pokeapi-evolution", key = "#chainId")
	public EvolutionChainJson fetchEvolutionChain(String chainId) {
		return restClient.get()
				.uri("/evolution-chain/{chainId}", chainId)
				.retrieve()
				.onStatus(status -> status.value() == 404, (request, response) -> {
					throw new PokemonNotFoundException("evolution-chain/" + chainId);
				})
				.body(EvolutionChainJson.class);
	}

	@Cacheable(cacheNames = "pokeapi-pokemon-list", key = "#offset + '-' + #limit")
	public PokemonListJson fetchPokemonList(int offset, int limit) {
		return restClient.get()
				.uri(uriBuilder -> uriBuilder
						.path("/pokemon")
						.queryParam("offset", offset)
						.queryParam("limit", limit)
						.build())
				.retrieve()
				.body(PokemonListJson.class);
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record PokemonListJson(long count, List<NamedResourceJson> results) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record NamedResourceJson(String name, String url) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record PokemonJson(
			String name,
			Integer height,
			Integer weight,
			SpritesJson sprites,
			List<TypeSlotJson> types,
			List<StatSlotJson> stats,
			List<AbilitySlotJson> abilities,
			NamedResourceJson species) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record SpritesJson(
			@JsonProperty("front_default") String frontDefault,
			SpritesOtherJson other) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record SpritesOtherJson(
			@JsonProperty("official-artwork") OfficialArtworkJson officialArtwork) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record OfficialArtworkJson(
			@JsonProperty("front_default") String frontDefault) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record TypeSlotJson(int slot, NamedResourceJson type) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record StatSlotJson(
			@JsonProperty("base_stat") int baseStat,
			NamedResourceJson stat) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record AbilitySlotJson(
			NamedResourceJson ability,
			@JsonProperty("is_hidden") boolean hidden,
			int slot) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record SpeciesJson(
			String name,
			List<GenusJson> genera,
			@JsonProperty("flavor_text_entries") List<FlavorTextJson> flavorTextEntries,
			@JsonProperty("evolution_chain") NamedResourceJson evolutionChain) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record GenusJson(String genus, NamedResourceJson language) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record FlavorTextJson(
			@JsonProperty("flavor_text") String flavorText,
			NamedResourceJson language) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record EvolutionChainJson(ChainLinkJson chain) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record ChainLinkJson(
			NamedResourceJson species,
			@JsonProperty("evolves_to") List<ChainLinkJson> evolvesTo) {
	}
}
