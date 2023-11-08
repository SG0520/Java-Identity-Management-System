 package com.jwt.identity.dto;

import lombok.Data;

@Data
public class PasswordChangeRequest {
	private String oldPassword;
	private String newPassword;
	private String reNewPassword;

}
 //ses-smtp-user.20230605-160713