package com.jwt.identity.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponseDto {

	private String firstName;
	private String lastName;
	private String email;
	private String userClass;
	private String telephone;
	private String gender;
	private Integer tenantId;
	private String tenantName;
	private String title;
	private String roleId;

}
