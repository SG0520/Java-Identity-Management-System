package com.jwt.identity.aspects.exceptions;

public class UnauthorizedException extends RuntimeException {

	private static final long serialVersionUID = 6676991862642514371L;

	public UnauthorizedException() {
		super();
	}

	public UnauthorizedException(final String message) {
		super(message);
	}

}
