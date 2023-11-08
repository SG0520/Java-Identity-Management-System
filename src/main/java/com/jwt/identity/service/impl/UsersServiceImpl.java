package com.jwt.identity.service.impl;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.jwt.identity.aspects.exceptions.NotFoundException;
import com.jwt.identity.dto.UserDto;
import com.jwt.identity.entity.Tenant;
import com.jwt.identity.entity.UserRole;
import com.jwt.identity.entity.Users;
import com.jwt.identity.repository.TenantRepository;
import com.jwt.identity.repository.UserRoleRepository;
import com.jwt.identity.repository.UsersRepository;
import com.jwt.identity.service.UsersService;
import com.jwt.identity.util.PasswordService;
import com.jwt.identity.util.XlsxHelper;

@Service
public class UsersServiceImpl implements UsersService {

	@Autowired
	private UsersRepository usersRepository;

	@Autowired
	private UserRoleRepository userRoleRepository;

	@Autowired
	private TenantRepository tenantRepository;

	@Autowired
	private PasswordService passwordEncoder;

	@Autowired
	private XlsxHelper excelHelper;

	public Users createUser(UserDto userDto) {

		Users newUser = prepareUserEntity(userDto);
		newUser.setCreatedBy("System Generated");
		newUser.setModifiedBy("System Generated");
		newUser.setCreatedDate(new Date());
		newUser.setModifiedDate(new Date());
		UserRole userRole = userRoleRepository.findById("LEARNER").orElse(null);
		if (userRole == null) {
			throw new NotFoundException("UserRole Not Found");
		}
		newUser.setUserRole(userRole);
		newUser.setPassword(passwordEncoder.encode(newUser.getPassword()));
		usersRepository.save(newUser);
		return newUser;
	}

	public void deleteUserById(String userGuid) {
		usersRepository.deleteById(userGuid);
	}

	public Users findUserByEmailAndTenantId(String userEmail, int tenantId) {
		return usersRepository.findUserByEmailAndTenantId(userEmail, tenantId);
	}

	@Override
	public Users findUserByEmail(String email) {
		return usersRepository.findByEmail(email);
	}

	private Users prepareUserEntity(UserDto userDto) {
		Users user = new Users();
		user.setFirstName(userDto.getFirstName());
		user.setLastName(userDto.getLastName());
		String userGuid = java.util.UUID.randomUUID().toString();
		user.setUserGuid(userGuid);
		user.setPassword(userDto.getPassword());
		user.setEmail(userDto.getEmail());
		user.setUserStatus("ACT");
		user.setTelephone(userDto.getTelephone());
		if (userDto.getTenantId() != null)
			user.setTenantId(userDto.getTenantId());
		else {
			if (!StringUtils.isBlank(userDto.getTenantName())) {
				Tenant tenantObj = tenantRepository.findByTenantName(userDto.getTenantName());
				user.setTenantId(tenantObj.getTenantId());
			} else
				throw new NotFoundException("Tenant Not Found");
		}
		user.setTitle(StringUtils.isBlank(userDto.getTitle())?"None":userDto.getTitle());
		return user;
	}

	@Override
	public void registerBulkUser(MultipartFile file, Users supUser) {
		try {
			List<UserDto> userDtoList = excelHelper.excelToUserDtoList(file.getInputStream(), supUser.getTenantId());
			for (UserDto userDtoObj : userDtoList) {
				UserRole userRole = userRoleRepository.findById(userDtoObj.getRoleName()).orElse(null);
				if (userRole == null) {
					throw new NotFoundException("No Such Role Found " + userDtoObj.getRoleName());
				}
			}
			for (UserDto userDtoObj : userDtoList) {
				userDtoObj.setTenantId(supUser.getTenantId());
				Users newUser = prepareUserEntity(userDtoObj);
				newUser.setCreatedBy(supUser.getUserGuid());
				newUser.setModifiedBy(supUser.getUserGuid());
				newUser.setCreatedDate(new Date());
				newUser.setModifiedDate(new Date());
				UserRole userRole = userRoleRepository.findById(userDtoObj.getRoleName()).orElse(null);
				newUser.setUserRole(userRole);
				boolean isValidUserItem = true;
				String statusText = "";
				if (StringUtils.isBlank(newUser.getEmail())) {
					statusText += "Invalid Data for Email";
					isValidUserItem = false;
				}
				if (StringUtils.isBlank(newUser.getFirstName())) {
					statusText += "Invalid Data for FirstName";
					isValidUserItem = false;
				}
				if (StringUtils.isBlank(newUser.getTelephone())) {
					statusText += "Invalid Data for Mobile";
					isValidUserItem = false;
				}
				if (StringUtils.isBlank(newUser.getPassword())) {
					statusText += "Invalid Data for Password";
					isValidUserItem = false;
				} else if (newUser.getPassword().length() < 6) {
					statusText += "Character length should be atleast 6!";
					isValidUserItem = false;
				}

				if (isValidUserItem && statusText.isEmpty()) {
					newUser.setPassword(passwordEncoder.encode(newUser.getPassword()));
					System.out.println(newUser.toString());
					usersRepository.save(newUser);
				}
			}
		} catch (IOException e) {
			throw new RuntimeException("fail to store csv data: " + e.getMessage());
		}
	}

	@Override
	public void changePassword(Users user) {
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		usersRepository.save(user);
	}
}
