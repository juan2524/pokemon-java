package com.sauldaniel.pokemon.adapter.in.web;

import com.sauldaniel.pokemon.application.service.AuthService;
import com.sauldaniel.pokemon.domain.exception.ConflictException;
import com.sauldaniel.pokemon.domain.model.Role;
import com.sauldaniel.pokemon.domain.model.UserAccount;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@Import({
		GlobalExceptionHandler.class,
		com.sauldaniel.pokemon.config.SecurityConfig.class,
		com.sauldaniel.pokemon.config.JwtConfig.class
})
@TestPropertySource(properties = "app.security.jwt-secret=test-secret-key-at-least-32-chars!!")
class AuthControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private AuthService authService;

	@Test
	void registersNewUserAndReturnsCreatedProfile() throws Exception {
		UUID id = UUID.randomUUID();
		when(authService.register("trainer@example.com", "password123"))
				.thenReturn(new UserAccount(id, "trainer@example.com", "hash", Role.USER, Instant.parse("2024-06-01T00:00:00Z")));

		mockMvc.perform(post("/api/v1/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"trainer@example.com","password":"password123"}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(id.toString()))
				.andExpect(jsonPath("$.email").value("trainer@example.com"))
				.andExpect(jsonPath("$.role").value("USER"));
	}

	@Test
	void rejectsInvalidRegistrationPayload() throws Exception {
		mockMvc.perform(post("/api/v1/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"not-an-email","password":"short"}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.errors").isArray());
	}

	@Test
	void returns409WhenEmailAlreadyRegistered() throws Exception {
		when(authService.register(anyString(), anyString()))
				.thenThrow(new ConflictException("Email already registered"));

		mockMvc.perform(post("/api/v1/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"demo@example.com","password":"password123"}
								"""))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.status").value(409))
				.andExpect(jsonPath("$.detail").value("Email already registered"));
	}

	@Test
	void logsInWithValidCredentials() throws Exception {
		when(authService.login("admin@example.com", "password123"))
				.thenReturn(new AuthService.LoginResult("jwt-token", "Bearer", 3600, Role.ADMIN, "admin@example.com"));

		mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"admin@example.com","password":"password123"}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accessToken").value("jwt-token"))
				.andExpect(jsonPath("$.tokenType").value("Bearer"))
				.andExpect(jsonPath("$.role").value("ADMIN"))
				.andExpect(jsonPath("$.email").value("admin@example.com"));
	}

	@Test
	void returns401ForInvalidCredentials() throws Exception {
		when(authService.login(anyString(), anyString()))
				.thenThrow(new BadCredentialsException("Invalid credentials"));

		mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"admin@example.com","password":"wrong-password"}
								"""))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.status").value(401))
				.andExpect(jsonPath("$.detail").value("Invalid credentials"));
	}
}
