package com.jwt.identity.dto;

import lombok.Data;

@Data
public class ResetPasswordUserDetailsDto {
	private String firstName;
	private String lastName;
	private String email;
}
