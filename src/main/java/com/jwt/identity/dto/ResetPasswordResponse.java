package com.jwt.identity.dto;

import lombok.Data;

@Data
public class ResetPasswordResponse {
  private String message;
  private ResetPasswordUserDetailsDto resetPasswordUserDetails;
}
