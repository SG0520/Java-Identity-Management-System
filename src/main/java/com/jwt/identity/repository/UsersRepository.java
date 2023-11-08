package com.jwt.identity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.jwt.identity.entity.Users;

public interface UsersRepository extends JpaRepository<Users, String> {

	@Query(value = "SELECT * FROM users WHERE users.EMAIL=?1 AND users.TENANT_ID=?2", nativeQuery = true)
	Users findUserByEmailAndTenantId(String userEmail, int tenantId);

//	@Query(value = "SELECT * FROM users WHERE users.USER_NAME=?1 AND users.PASSWORD=?2 AND users.TENANT_ID=?3", nativeQuery = true)
//	public Users authenticateUser(String userName, String password, int tenantId);

	Users findByEmail(String email);
}
