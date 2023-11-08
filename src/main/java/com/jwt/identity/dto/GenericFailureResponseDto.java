package com.jwt.identity.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GenericFailureResponseDto {

	@JsonProperty("status")
	private boolean status;

	@JsonProperty("message")
	private String message;

	public GenericFailureResponseDto(boolean status, String message) {
		super();
		this.status = status;
		this.message = message;
	}

	public boolean isStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public String toString() {
		return "GenericFailureResponseDto [status=" + status + ", message=" + message + "]";
	}

}
