package com.sauldaniel.pokemon.domain.exception;

public class InvalidPaginationException extends RuntimeException {

	public InvalidPaginationException(String message) {
		super(message);
	}
}
