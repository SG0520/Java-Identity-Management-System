package com.jwt.identity.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jwt.identity.entity.UserRole;


public interface UserRoleRepository extends JpaRepository<UserRole, String> {
	UserRole findByUserRoleName(String typeName);

	
}
