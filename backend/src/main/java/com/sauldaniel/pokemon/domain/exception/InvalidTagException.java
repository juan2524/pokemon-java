package com.sauldaniel.pokemon.domain.exception;

public class InvalidTagException extends RuntimeException {

	public InvalidTagException(String tag) {
		super("Unknown tag: " + tag);
	}
}
