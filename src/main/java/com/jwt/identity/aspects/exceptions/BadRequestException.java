package com.jwt.identity.aspects.exceptions;

public class BadRequestException extends RuntimeException {

	private static final long serialVersionUID = 6676991862642514371L;

	public BadRequestException() {
		super();
	}

	public BadRequestException(final String message) {
		super(message);
	}

}
