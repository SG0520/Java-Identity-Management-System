package com.jwt.identity.service;

import org.springframework.web.multipart.MultipartFile;

import com.jwt.identity.dto.UserDto;
import com.jwt.identity.entity.Users;

public interface UsersService {

	public Users findUserByEmailAndTenantId(String userId, int tenantId);

	public void deleteUserById(String userGuid);

	public Users findUserByEmail(String email);

	public Users createUser(UserDto userDto);

	public void registerBulkUser(MultipartFile file,  Users supUser);

	public void changePassword(Users user);

	

}
