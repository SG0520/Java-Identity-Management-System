package com.jwt.identity.aspects.exceptions;

public class NotFoundException extends RuntimeException {

	private static final long serialVersionUID = 6676991862642514371L;

	public NotFoundException() {
		super();
	}

	public NotFoundException(final String message) {
		super(message);
	}

}
