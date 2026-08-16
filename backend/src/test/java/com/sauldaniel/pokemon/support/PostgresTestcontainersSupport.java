package com.sauldaniel.pokemon.support;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.PostgreSQLContainer;

import java.io.IOException;

/**
 * Prefers Testcontainers PostgreSQL when Docker is available; otherwise falls back to
 * embedded PostgreSQL so the suite can still run in restricted environments.
 */
public abstract class PostgresTestcontainersSupport {

	private static final boolean DOCKER_AVAILABLE = isDockerAvailable();
	private static final PostgreSQLContainer<?> TESTCONTAINERS_POSTGRES;
	private static final EmbeddedPostgres EMBEDDED_POSTGRES;
	private static final String JDBC_URL;
	private static final String USERNAME;
	private static final String PASSWORD;

	static {
		if (DOCKER_AVAILABLE) {
			TESTCONTAINERS_POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
					.withDatabaseName("pokemon")
					.withUsername("pokemon")
					.withPassword("pokemon");
			TESTCONTAINERS_POSTGRES.start();
			EMBEDDED_POSTGRES = null;
			JDBC_URL = TESTCONTAINERS_POSTGRES.getJdbcUrl();
			USERNAME = TESTCONTAINERS_POSTGRES.getUsername();
			PASSWORD = TESTCONTAINERS_POSTGRES.getPassword();
		}
		else {
			TESTCONTAINERS_POSTGRES = null;
			try {
				EMBEDDED_POSTGRES = EmbeddedPostgres.builder().start();
			}
			catch (IOException ex) {
				throw new IllegalStateException("Unable to start embedded PostgreSQL fallback", ex);
			}
			JDBC_URL = EMBEDDED_POSTGRES.getJdbcUrl("postgres", "postgres");
			USERNAME = "postgres";
			PASSWORD = "postgres";
		}
	}

	@DynamicPropertySource
	static void registerDatasource(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", () -> JDBC_URL);
		registry.add("spring.datasource.username", () -> USERNAME);
		registry.add("spring.datasource.password", () -> PASSWORD);
	}

	private static boolean isDockerAvailable() {
		try {
			DockerClientFactory.instance().client();
			return true;
		}
		catch (Throwable ex) {
			return false;
		}
	}
}
