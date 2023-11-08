package com.jwt.identity.service.impl;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.jwt.identity.entity.UserRole;
import com.jwt.identity.entity.Users;
import com.jwt.identity.repository.UsersRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {
	@Autowired
	private UsersRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

		Users userObj = userRepository.findByEmail(email);

		if (userObj == null) {
			throw new UsernameNotFoundException("User Not Found " + email);
		} else {
			UserRole userRoleObj = userObj.getUserRole();
			return new User(userObj.getEmail(), userObj.getPassword(),
					Arrays.asList(new SimpleGrantedAuthority("ROLE_" + userRoleObj.getUserRoleId())));
		}
	}

}
