package com.jwt.identity.dto;

import lombok.Data;

@Data
public class PasswordResetRequest {
	private String newPassword;
	private String reNewPassword;

}
