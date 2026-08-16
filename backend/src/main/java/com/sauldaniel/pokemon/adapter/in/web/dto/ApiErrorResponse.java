package com.sauldaniel.pokemon.adapter.in.web.dto;

import java.util.List;

public record ApiErrorResponse(
		String type,
		String title,
		int status,
		String detail,
		String instance,
		List<FieldError> errors
) {

	public record FieldError(String field, String message) {
	}
}
