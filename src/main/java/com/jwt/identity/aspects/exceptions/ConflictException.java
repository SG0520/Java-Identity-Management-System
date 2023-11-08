package com.jwt.identity.aspects.exceptions;

public class ConflictException extends RuntimeException{

	private static final long serialVersionUID = -8765230873447478695L;
	
	public ConflictException() {
		super();
	}

	public ConflictException(final String message) {
		super(message);
	}
}

         