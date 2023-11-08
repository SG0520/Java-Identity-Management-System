package com.jwt.identity.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.Data;

@Service
@Data
public class PasswordService {
	private PasswordEncoder passwordEncoder;

	PasswordService() {
		passwordEncoder = new BCryptPasswordEncoder();
	}

	public boolean matches(String rawPassword, String encodedPassword) {
		return passwordEncoder.matches(rawPassword, encodedPassword);
	}

	public String encode(String password) {
		return passwordEncoder.encode(password);
	}

}
