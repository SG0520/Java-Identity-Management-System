package com.jwt.identity.service;

import javax.validation.constraints.NotNull;

import org.springframework.stereotype.Service;

import com.jwt.identity.entity.ForgotPasswordToken;
import com.jwt.identity.entity.Users;

@Service
public interface ForgotPasswordTokenService {

	ForgotPasswordToken createPasswordToken(Users user);

	ForgotPasswordToken getByPasswordToken(String passwordToken);

	void deletePasswordToken(@NotNull String passwordToken);

}
                                           
 