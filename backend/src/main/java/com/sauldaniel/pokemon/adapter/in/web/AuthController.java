package com.sauldaniel.pokemon.adapter.in.web;

import com.sauldaniel.pokemon.adapter.in.web.dto.LoginRequest;
import com.sauldaniel.pokemon.adapter.in.web.dto.LoginResponse;
import com.sauldaniel.pokemon.adapter.in.web.dto.RegisterRequest;
import com.sauldaniel.pokemon.adapter.in.web.dto.RegisterResponse;
import com.sauldaniel.pokemon.application.service.AuthService;
import com.sauldaniel.pokemon.domain.model.UserAccount;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/register")
	@ResponseStatus(HttpStatus.CREATED)
	public RegisterResponse register(@Valid @RequestBody RegisterRequest request) {
		UserAccount user = authService.register(request.email(), request.password());
		return new RegisterResponse(user.id().toString(), user.email(), user.role().name());
	}

	@PostMapping("/login")
	public LoginResponse login(@Valid @RequestBody LoginRequest request) {
		AuthService.LoginResult result = authService.login(request.email(), request.password());
		return new LoginResponse(
				result.accessToken(),
				result.tokenType(),
				result.expiresIn(),
				result.role().name(),
				result.email());
	}
}
