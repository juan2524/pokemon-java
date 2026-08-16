package com.sauldaniel.pokemon.adapter.out.pokeapi;

import com.sauldaniel.pokemon.adapter.out.pokeapi.PokeApiResourceClient.AbilitySlotJson;
import com.sauldaniel.pokemon.adapter.out.pokeapi.PokeApiResourceClient.ChainLinkJson;
import com.sauldaniel.pokemon.adapter.out.pokeapi.PokeApiResourceClient.EvolutionChainJson;
import com.sauldaniel.pokemon.adapter.out.pokeapi.PokeApiResourceClient.FlavorTextJson;
import com.sauldaniel.pokemon.adapter.out.pokeapi.PokeApiResourceClient.PokemonJson;
import com.sauldaniel.pokemon.adapter.out.pokeapi.PokeApiResourceClient.PokemonListJson;
import com.sauldaniel.pokemon.adapter.out.pokeapi.PokeApiResourceClient.SpeciesJson;
import com.sauldaniel.pokemon.adapter.out.pokeapi.PokeApiResourceClient.SpritesJson;
import com.sauldaniel.pokemon.adapter.out.pokeapi.PokeApiResourceClient.StatSlotJson;
import com.sauldaniel.pokemon.adapter.out.pokeapi.PokeApiResourceClient.TypeSlotJson;
import com.sauldaniel.pokemon.domain.exception.PokemonNotFoundException;
import com.sauldaniel.pokemon.domain.model.EvolutionNode;
import com.sauldaniel.pokemon.domain.model.PokemonCard;
import com.sauldaniel.pokemon.domain.model.PokemonDetail;
import com.sauldaniel.pokemon.domain.model.PokemonNamePage;
import com.sauldaniel.pokemon.domain.model.PokemonStat;
import com.sauldaniel.pokemon.domain.port.out.PokeApiClient;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
public class HttpPokeApiClient implements PokeApiClient {

	private final PokeApiResourceClient resources;

	public HttpPokeApiClient(PokeApiResourceClient resources) {
		this.resources = resources;
	}

	@Override
	public PokemonNamePage listPokemonNames(int offset, int limit) {
		PokemonListJson response = resources.fetchPokemonList(offset, limit);
		if (response == null || response.results() == null) {
			return new PokemonNamePage(List.of(), 0);
		}
		List<String> names = response.results().stream()
				.map(result -> result.name())
				.toList();
		return new PokemonNamePage(names, response.count());
	}

	@Override
	public PokemonCard getPokemonCard(String name) {
		PokemonJson pokemon = requirePokemon(name);
		SpeciesJson species = resources.fetchSpecies(speciesKey(pokemon, name));
		return new PokemonCard(
				pokemon.name(),
				resolveImage(pokemon.sprites()),
				resolveCategory(species),
				toKg(pokemon.weight()),
				resolveAbilities(pokemon.abilities()));
	}

	@Override
	public PokemonDetail getPokemonDetail(String idOrName) {
		PokemonJson pokemon = requirePokemon(idOrName);
		SpeciesJson species = resources.fetchSpecies(speciesKey(pokemon, idOrName));
		List<EvolutionNode> lineage = resolveEvolutionLineage(species);

		return new PokemonDetail(
				pokemon.name(),
				resolveImage(pokemon.sprites()),
				toMeters(pokemon.height()),
				toKg(pokemon.weight()),
				resolveTypes(pokemon.types()),
				resolveStats(pokemon.stats()),
				resolveFlavorText(species),
				lineage);
	}

	private static double toMeters(Integer heightDm) {
		return heightDm == null ? 0.0 : heightDm / 10.0;
	}

	private static double toKg(Integer weightHg) {
		return weightHg == null ? 0.0 : weightHg / 10.0;
	}

	private PokemonJson requirePokemon(String idOrName) {
		PokemonJson pokemon = resources.fetchPokemon(idOrName);
		if (pokemon == null) {
			throw new PokemonNotFoundException(idOrName);
		}
		return pokemon;
	}

	private static String speciesKey(PokemonJson pokemon, String fallback) {
		if (pokemon.species() != null && pokemon.species().name() != null) {
			return pokemon.species().name();
		}
		return fallback;
	}

	private List<EvolutionNode> resolveEvolutionLineage(SpeciesJson species) {
		String chainId = extractEvolutionChainId(species);
		if (chainId == null) {
			return List.of();
		}
		EvolutionChainJson chain = resources.fetchEvolutionChain(chainId);
		if (chain == null || chain.chain() == null) {
			return List.of();
		}
		List<EvolutionNode> lineage = new ArrayList<>();
		walkEvolution(chain.chain(), lineage);
		return List.copyOf(lineage);
	}

	private static void walkEvolution(ChainLinkJson node, List<EvolutionNode> lineage) {
		if (node == null || node.species() == null || node.species().name() == null) {
			return;
		}
		lineage.add(new EvolutionNode(node.species().name()));
		if (node.evolvesTo() == null) {
			return;
		}
		for (ChainLinkJson child : node.evolvesTo()) {
			walkEvolution(child, lineage);
		}
	}

	private static String extractEvolutionChainId(SpeciesJson species) {
		if (species == null || species.evolutionChain() == null || species.evolutionChain().url() == null) {
			return null;
		}
		String url = species.evolutionChain().url();
		String trimmed = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
		int slash = trimmed.lastIndexOf('/');
		return slash >= 0 ? trimmed.substring(slash + 1) : trimmed;
	}

	private static String resolveImage(SpritesJson sprites) {
		if (sprites == null) {
			return null;
		}
		if (sprites.other() != null
				&& sprites.other().officialArtwork() != null
				&& sprites.other().officialArtwork().frontDefault() != null) {
			return sprites.other().officialArtwork().frontDefault();
		}
		return sprites.frontDefault();
	}

	private static String resolveCategory(SpeciesJson species) {
		if (species == null || species.genera() == null) {
			return null;
		}
		return species.genera().stream()
				.filter(genus -> genus.language() != null && "en".equals(genus.language().name()))
				.map(genus -> genus.genus())
				.findFirst()
				.orElse(null);
	}

	private static String resolveFlavorText(SpeciesJson species) {
		if (species == null || species.flavorTextEntries() == null) {
			return null;
		}
		return species.flavorTextEntries().stream()
				.filter(entry -> entry.language() != null && "en".equals(entry.language().name()))
				.map(FlavorTextJson::flavorText)
				.filter(text -> text != null && !text.isBlank())
				.map(HttpPokeApiClient::normalizeFlavorText)
				.findFirst()
				.orElse(null);
	}

	private static String normalizeFlavorText(String flavorText) {
		return flavorText
				.replace("\f", " ")
				.replace('\n', ' ')
				.replaceAll("\\s+", " ")
				.trim();
	}

	private static List<String> resolveAbilities(List<AbilitySlotJson> abilities) {
		if (abilities == null) {
			return List.of();
		}
		return abilities.stream()
				.sorted(Comparator.comparingInt(AbilitySlotJson::slot))
				.map(ability -> ability.ability().name())
				.toList();
	}

	private static List<String> resolveTypes(List<TypeSlotJson> types) {
		if (types == null) {
			return List.of();
		}
		return types.stream()
				.sorted(Comparator.comparingInt(TypeSlotJson::slot))
				.map(type -> type.type().name())
				.toList();
	}

	private static List<PokemonStat> resolveStats(List<StatSlotJson> stats) {
		if (stats == null) {
			return List.of();
		}
		return stats.stream()
				.map(stat -> new PokemonStat(stat.stat().name(), stat.baseStat()))
				.toList();
	}
}
