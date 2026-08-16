package com.sauldaniel.pokemon.adapter.in.web;

import com.sauldaniel.pokemon.adapter.in.web.dto.ApiErrorResponse;
import com.sauldaniel.pokemon.domain.exception.ConflictException;
import com.sauldaniel.pokemon.domain.exception.InvalidPaginationException;
import com.sauldaniel.pokemon.domain.exception.InvalidTagException;
import com.sauldaniel.pokemon.domain.exception.LocalPokemonNotFoundException;
import com.sauldaniel.pokemon.domain.exception.PokemonNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ApiErrorResponse> handleConstraintViolation(
			ConstraintViolationException ex,
			HttpServletRequest request) {
		List<ApiErrorResponse.FieldError> errors = ex.getConstraintViolations().stream()
				.map(violation -> new ApiErrorResponse.FieldError(
						extractField(violation.getPropertyPath().toString()),
						violation.getMessage()))
				.toList();
		return build(HttpStatus.BAD_REQUEST, "Validation failed", "Request validation failed", request, errors);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(
			MethodArgumentNotValidException ex,
			HttpServletRequest request) {
		List<ApiErrorResponse.FieldError> errors = ex.getBindingResult().getFieldErrors().stream()
				.map(error -> new ApiErrorResponse.FieldError(error.getField(), error.getDefaultMessage()))
				.toList();
		return build(HttpStatus.BAD_REQUEST, "Validation failed", "Request validation failed", request, errors);
	}

	@ExceptionHandler(InvalidPaginationException.class)
	public ResponseEntity<ApiErrorResponse> handleInvalidPagination(
			InvalidPaginationException ex,
			HttpServletRequest request) {
		return build(HttpStatus.BAD_REQUEST, "Validation failed", ex.getMessage(), request, List.of());
	}

	@ExceptionHandler(InvalidTagException.class)
	public ResponseEntity<ApiErrorResponse> handleInvalidTag(
			InvalidTagException ex,
			HttpServletRequest request) {
		return build(HttpStatus.BAD_REQUEST, "Validation failed", ex.getMessage(), request, List.of());
	}

	@ExceptionHandler({PokemonNotFoundException.class, LocalPokemonNotFoundException.class})
	public ResponseEntity<ApiErrorResponse> handleNotFound(
			RuntimeException ex,
			HttpServletRequest request) {
		return build(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), request, List.of());
	}

	@ExceptionHandler(BadCredentialsException.class)
	public ResponseEntity<ApiErrorResponse> handleBadCredentials(
			BadCredentialsException ex,
			HttpServletRequest request) {
		return build(HttpStatus.UNAUTHORIZED, "Unauthorized", "Invalid credentials", request, List.of());
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ApiErrorResponse> handleAccessDenied(
			AccessDeniedException ex,
			HttpServletRequest request) {
		return build(HttpStatus.FORBIDDEN, "Forbidden", "Access denied", request, List.of());
	}

	@ExceptionHandler({
			ConflictException.class,
			ObjectOptimisticLockingFailureException.class,
			DataIntegrityViolationException.class
	})
	public ResponseEntity<ApiErrorResponse> handleConflict(
			Exception ex,
			HttpServletRequest request) {
		String detail = ex instanceof ConflictException
				? ex.getMessage()
				: "Conflict while persisting Pokémon data";
		return build(HttpStatus.CONFLICT, "Conflict", detail, request, List.of());
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
		return build(
				HttpStatus.INTERNAL_SERVER_ERROR,
				"Internal Server Error",
				"An unexpected error occurred",
				request,
				List.of());
	}

	private static ResponseEntity<ApiErrorResponse> build(
			HttpStatus status,
			String title,
			String detail,
			HttpServletRequest request,
			List<ApiErrorResponse.FieldError> errors) {
		ApiErrorResponse body = new ApiErrorResponse(
				"about:blank",
				title,
				status.value(),
				detail,
				request.getRequestURI(),
				errors);
		return ResponseEntity.status(status).body(body);
	}

	private static String extractField(String propertyPath) {
		int lastDot = propertyPath.lastIndexOf('.');
		return lastDot >= 0 ? propertyPath.substring(lastDot + 1) : propertyPath;
	}
}
