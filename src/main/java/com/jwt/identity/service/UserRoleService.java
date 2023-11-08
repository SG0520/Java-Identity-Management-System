package com.jwt.identity.service;

import com.jwt.identity.entity.UserRole;

public interface UserRoleService {

	UserRole getByRoleName(String roleName);

}
