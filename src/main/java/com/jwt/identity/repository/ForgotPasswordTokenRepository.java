package com.jwt.identity.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jwt.identity.entity.ForgotPasswordToken;

public interface ForgotPasswordTokenRepository extends JpaRepository<ForgotPasswordToken, String> {

	
}
