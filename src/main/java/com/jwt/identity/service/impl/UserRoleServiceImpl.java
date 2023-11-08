package com.jwt.identity.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jwt.identity.entity.UserRole;
import com.jwt.identity.repository.UserRoleRepository;
import com.jwt.identity.service.UserRoleService;

@Service
public class UserRoleServiceImpl implements UserRoleService {

	@Autowired
	UserRoleRepository userRoleRepository;

	@Override
	public UserRole getByRoleName(String roleName) {
		return userRoleRepository.findByUserRoleName(roleName);

	}

}
