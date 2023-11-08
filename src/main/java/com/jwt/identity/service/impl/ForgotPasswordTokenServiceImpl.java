package com.jwt.identity.service.impl;

import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jwt.identity.entity.ForgotPasswordToken;
import com.jwt.identity.entity.Users;
import com.jwt.identity.repository.ForgotPasswordTokenRepository;
import com.jwt.identity.service.ForgotPasswordTokenService;

@Service
public class ForgotPasswordTokenServiceImpl implements ForgotPasswordTokenService {

	@Autowired
	private ForgotPasswordTokenRepository forgotPasswordTokenRepository;

	@Override
	public ForgotPasswordToken createPasswordToken(Users user) {
		ForgotPasswordToken forgotPasswordTokenObj = new ForgotPasswordToken();
		forgotPasswordTokenObj.setPasswordToken(UUID.randomUUID().toString());
		forgotPasswordTokenObj.setTenantId(user.getTenantId());
		forgotPasswordTokenObj.setEmail(user.getEmail());
		forgotPasswordTokenObj.setUserGuid(user.getUserGuid());
		forgotPasswordTokenObj.setCreatedDate(new Date());
		forgotPasswordTokenObj.setExpiryDate(
				Date.from(forgotPasswordTokenObj.getCreatedDate().toInstant().atZone(ZoneId.systemDefault())
						.toLocalDateTime().plusHours(24).atZone(ZoneId.systemDefault()).toInstant()));
		return forgotPasswordTokenRepository.save(forgotPasswordTokenObj);

	}

	@Override
	public ForgotPasswordToken getByPasswordToken(String passwordToken) {

		return forgotPasswordTokenRepository.findById(passwordToken).orElse(null);
	}

	@Override
	public void deletePasswordToken(@NotNull String passwordToken) {
		forgotPasswordTokenRepository.deleteById(passwordToken);

	}

}
