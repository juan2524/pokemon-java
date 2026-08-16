package com.sauldaniel.pokemon.adapter.out.pokeapi;

import com.sauldaniel.pokemon.domain.exception.PokemonNotFoundException;
import com.sauldaniel.pokemon.domain.model.PokemonCard;
import com.sauldaniel.pokemon.domain.model.PokemonDetail;
import com.sauldaniel.pokemon.domain.model.PokemonNamePage;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HttpPokeApiClientTest {

	private MockWebServer server;
	private HttpPokeApiClient client;

	@BeforeEach
	void setUp() throws IOException {
		server = new MockWebServer();
		server.start();
		RestClient restClient = RestClient.builder()
				.baseUrl(server.url("/").toString().replaceAll("/$", ""))
				.build();
		client = new HttpPokeApiClient(new PokeApiResourceClient(restClient));
	}

	@AfterEach
	void tearDown() throws IOException {
		server.shutdown();
	}

	@Test
	void listPokemonNamesMapsPaginatedResults() throws InterruptedException {
		server.enqueue(new MockResponse()
				.setBody("""
						{
						  "count": 1302,
						  "results": [
						    {"name": "bulbasaur", "url": "https://pokeapi.co/api/v2/pokemon/1/"},
						    {"name": "ivysaur", "url": "https://pokeapi.co/api/v2/pokemon/2/"}
						  ]
						}
						""")
				.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

		PokemonNamePage page = client.listPokemonNames(0, 2);

		assertThat(page.totalCount()).isEqualTo(1302);
		assertThat(page.names()).containsExactly("bulbasaur", "ivysaur");

		RecordedRequest request = server.takeRequest(1, TimeUnit.SECONDS);
		assertThat(request).isNotNull();
		assertThat(request.getPath()).isEqualTo("/pokemon?offset=0&limit=2");
	}

	@Test
	void getPokemonCardMapsSpriteCategoryWeightAndAbilities() throws InterruptedException {
		server.enqueue(new MockResponse()
				.setBody("""
						{
						  "name": "bulbasaur",
						  "weight": 69,
						  "sprites": {
						    "front_default": "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/1.png",
						    "other": {
						      "official-artwork": {
						        "front_default": "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/1.png"
						      }
						    }
						  },
						  "abilities": [
						    {"ability": {"name": "overgrow"}, "is_hidden": false, "slot": 1},
						    {"ability": {"name": "chlorophyll"}, "is_hidden": true, "slot": 3}
						  ]
						}
						""")
				.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));
		server.enqueue(new MockResponse()
				.setBody("""
						{
						  "genera": [
						    {"genus": "たねポケモン", "language": {"name": "ja"}},
						    {"genus": "Seed Pokémon", "language": {"name": "en"}}
						  ]
						}
						""")
				.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

		PokemonCard card = client.getPokemonCard("bulbasaur");

		assertThat(card.name()).isEqualTo("bulbasaur");
		assertThat(card.spriteUrl()).isEqualTo(
				"https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/1.png");
		assertThat(card.category()).isEqualTo("Seed Pokémon");
		assertThat(card.weightKg()).isEqualTo(6.9);
		assertThat(card.abilities()).containsExactly("overgrow", "chlorophyll");

		RecordedRequest pokemonRequest = server.takeRequest(1, TimeUnit.SECONDS);
		RecordedRequest speciesRequest = server.takeRequest(1, TimeUnit.SECONDS);
		assertThat(pokemonRequest.getPath()).isEqualTo("/pokemon/bulbasaur");
		assertThat(speciesRequest.getPath()).isEqualTo("/pokemon-species/bulbasaur");
	}

	@Test
	void getPokemonCardFallsBackToFrontDefaultSprite() {
		server.enqueue(new MockResponse()
				.setBody("""
						{
						  "name": "missingno",
						  "weight": 10,
						  "sprites": {
						    "front_default": "https://example.com/front.png",
						    "other": {
						      "official-artwork": {
						        "front_default": null
						      }
						    }
						  },
						  "abilities": [
						    {"ability": {"name": "pressure"}, "is_hidden": false, "slot": 1}
						  ]
						}
						""")
				.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));
		server.enqueue(new MockResponse()
				.setBody("""
						{
						  "genera": [
						    {"genus": "Glitch Pokémon", "language": {"name": "en"}}
						  ]
						}
						""")
				.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

		PokemonCard card = client.getPokemonCard("missingno");

		assertThat(card.spriteUrl()).isEqualTo("https://example.com/front.png");
	}

	@Test
	void getPokemonDetailMapsCoreFieldsFlavorTextAndEvolutionLineage() throws InterruptedException {
		server.enqueue(new MockResponse()
				.setBody("""
						{
						  "name": "bulbasaur",
						  "height": 7,
						  "weight": 69,
						  "sprites": {
						    "front_default": "https://example.com/front.png",
						    "other": {
						      "official-artwork": {
						        "front_default": "https://example.com/official.png"
						      }
						    }
						  },
						  "types": [
						    {"slot": 1, "type": {"name": "grass"}},
						    {"slot": 2, "type": {"name": "poison"}}
						  ],
						  "stats": [
						    {"base_stat": 45, "stat": {"name": "hp"}},
						    {"base_stat": 49, "stat": {"name": "attack"}},
						    {"base_stat": 49, "stat": {"name": "defense"}},
						    {"base_stat": 65, "stat": {"name": "special-attack"}},
						    {"base_stat": 65, "stat": {"name": "special-defense"}},
						    {"base_stat": 45, "stat": {"name": "speed"}}
						  ],
						  "species": {"name": "bulbasaur", "url": "https://pokeapi.co/api/v2/pokemon-species/1/"},
						  "abilities": []
						}
						""")
				.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));
		server.enqueue(new MockResponse()
				.setBody("""
						{
						  "name": "bulbasaur",
						  "genera": [
						    {"genus": "Seed Pokémon", "language": {"name": "en"}}
						  ],
						  "flavor_text_entries": [
						    {"flavor_text": "Japanese text", "language": {"name": "ja"}},
						    {"flavor_text": "A strange seed was\\nplanted on its back.\\f", "language": {"name": "en"}},
						    {"flavor_text": "Later English entry", "language": {"name": "en"}}
						  ],
						  "evolution_chain": {"url": "https://pokeapi.co/api/v2/evolution-chain/1/"}
						}
						""")
				.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));
		server.enqueue(new MockResponse()
				.setBody("""
						{
						  "id": 1,
						  "chain": {
						    "species": {"name": "bulbasaur", "url": "https://pokeapi.co/api/v2/pokemon-species/1/"},
						    "evolves_to": [
						      {
						        "species": {"name": "ivysaur", "url": "https://pokeapi.co/api/v2/pokemon-species/2/"},
						        "evolves_to": [
						          {
						            "species": {"name": "venusaur", "url": "https://pokeapi.co/api/v2/pokemon-species/3/"},
						            "evolves_to": []
						          }
						        ]
						      }
						    ]
						  }
						}
						""")
				.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

		PokemonDetail detail = client.getPokemonDetail("1");

		assertThat(detail.name()).isEqualTo("bulbasaur");
		assertThat(detail.imageUrl()).isEqualTo("https://example.com/official.png");
		assertThat(detail.heightM()).isEqualTo(0.7);
		assertThat(detail.weightKg()).isEqualTo(6.9);
		assertThat(detail.types()).containsExactly("grass", "poison");
		assertThat(detail.stats()).extracting(stat -> stat.name() + ":" + stat.baseValue())
				.containsExactly(
						"hp:45",
						"attack:49",
						"defense:49",
						"special-attack:65",
						"special-defense:65",
						"speed:45");
		assertThat(detail.flavorTextEn()).isEqualTo("A strange seed was planted on its back.");
		assertThat(detail.evolutionLineage()).extracting(node -> node.name())
				.containsExactly("bulbasaur", "ivysaur", "venusaur");

		assertThat(server.takeRequest(1, TimeUnit.SECONDS).getPath()).isEqualTo("/pokemon/1");
		assertThat(server.takeRequest(1, TimeUnit.SECONDS).getPath()).isEqualTo("/pokemon-species/bulbasaur");
		assertThat(server.takeRequest(1, TimeUnit.SECONDS).getPath()).isEqualTo("/evolution-chain/1");
	}

	@Test
	void getPokemonDetailThrowsWhenPokemonMissingUpstream() {
		server.enqueue(new MockResponse().setResponseCode(404));

		assertThatThrownBy(() -> client.getPokemonDetail("missingno"))
				.isInstanceOf(PokemonNotFoundException.class)
				.hasMessageContaining("missingno");
	}

	@Test
	void getPokemonDetailWalksBranchingEvolutionTree() {
		server.enqueue(new MockResponse()
				.setBody("""
						{
						  "name": "eevee",
						  "height": 3,
						  "weight": 65,
						  "sprites": {"front_default": "https://example.com/eevee.png", "other": null},
						  "types": [{"slot": 1, "type": {"name": "normal"}}],
						  "stats": [{"base_stat": 55, "stat": {"name": "hp"}}],
						  "species": {"name": "eevee"},
						  "abilities": []
						}
						""")
				.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));
		server.enqueue(new MockResponse()
				.setBody("""
						{
						  "flavor_text_entries": [
						    {"flavor_text": "Its genetic code is irregular.", "language": {"name": "en"}}
						  ],
						  "evolution_chain": {"url": "https://pokeapi.co/api/v2/evolution-chain/67/"}
						}
						""")
				.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));
		server.enqueue(new MockResponse()
				.setBody("""
						{
						  "chain": {
						    "species": {"name": "eevee"},
						    "evolves_to": [
						      {"species": {"name": "vaporeon"}, "evolves_to": []},
						      {"species": {"name": "jolteon"}, "evolves_to": []},
						      {"species": {"name": "flareon"}, "evolves_to": []}
						    ]
						  }
						}
						""")
				.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

		PokemonDetail detail = client.getPokemonDetail("eevee");

		assertThat(detail.evolutionLineage()).extracting(node -> node.name())
				.containsExactly("eevee", "vaporeon", "jolteon", "flareon");
		assertThat(detail.imageUrl()).isEqualTo("https://example.com/eevee.png");
	}
}
