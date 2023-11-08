package com.jwt.identity.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@Data
@JsonInclude(value = Include.NON_NULL)
public class JwtTokenResponse {
	private String token;
	private String message;
	private UserResponseDto user;
}
