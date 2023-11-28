package com.jwt.identity.controller;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.jwt.identity.aspects.exceptions.BadRequestException;
import com.jwt.identity.aspects.exceptions.NotFoundException;
import com.jwt.identity.dto.JwtTokenResponse;
import com.jwt.identity.dto.UsersLoginDto;
import com.jwt.identity.entity.UserRole;
import com.jwt.identity.entity.Users;
import com.jwt.identity.service.BlackListedTokensService;
import com.jwt.identity.service.UsersService;
import com.jwt.identity.util.JwtTokenHelper;
import com.jwt.identity.util.PasswordService;
import com.jwt.identity.util.Utility;

public class AuthenticationControllerTest {

	@Mock
	private UsersService usersService;

	@Mock
	private JwtTokenHelper jwtTokenHelper;

	@Mock
	private BlackListedTokensService blackListedTokensService;

	@Mock
	private PasswordService passwordEncoder;

	@Mock
	private Utility utility;

	@InjectMocks
	private AuthenticationController authenticationController;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testLoginUser_ValidCredentials() {
		// Prepare test data
		UsersLoginDto userLoginDto = new UsersLoginDto();
		userLoginDto.setEmail("test@example.com");
		userLoginDto.setPassword("password123");
		Users user = new Users();
		user.setEmail("test@example.com");
		user.setPassword(passwordEncoder.encode("password123"));
		UserRole userRole = new UserRole();
		userRole.setUserRoleId("role_id");
		user.setUserRole(userRole);

		// Mock dependencies
		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		when(utility.getDomain(request)).thenReturn("valid_Domain");
		when(usersService.findUserByEmail(userLoginDto.getEmail())).thenReturn(user);
		when(passwordEncoder.matches(userLoginDto.getPassword(), user.getPassword())).thenReturn(true);
		when(jwtTokenHelper.generateToken(any(Users.class), anyString())).thenReturn("jwt_token");

		// Execute the method
		ResponseEntity<JwtTokenResponse> response = authenticationController.loginUser(userLoginDto, request);

		// Assert the response
		assertEquals(200, response.getStatusCodeValue());
		JwtTokenResponse tokenResponse = response.getBody();
		assertNotNull(tokenResponse);
		assertEquals("jwt_token", tokenResponse.getToken());
		assertNotNull(tokenResponse.getUser());
	}

	@Test(expected = NotFoundException.class)
	public void testLoginUser_UserNotFound() {
		// Prepare test data
		UsersLoginDto userLoginDto = new UsersLoginDto();
		userLoginDto.setEmail("test@example.com");
		userLoginDto.setPassword("password123");

		// Mock dependencies
		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		when(utility.getDomain(request)).thenReturn("valid_Domain");
		when(usersService.findUserByEmail(userLoginDto.getEmail())).thenReturn(null);

		// Execute the method (expecting NotFoundException)
		authenticationController.loginUser(userLoginDto, request);
	}

	@Test(expected = BadRequestException.class)
	public void testLoginUser_InvalidCredentials() {
		// Prepare test data

		UsersLoginDto userLoginDto = new UsersLoginDto();
		userLoginDto.setEmail("test@example.com");
		userLoginDto.setPassword("password123");
		Users user = new Users();
		user.setEmail("test@example.com");
		user.setPassword(passwordEncoder.encode("incorrect_password"));

		// Mock dependencies
		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		when(utility.getDomain(request)).thenReturn("valid_Domain");
		when(usersService.findUserByEmail(userLoginDto.getEmail())).thenReturn(user);
		when(passwordEncoder.matches(userLoginDto.getPassword(), user.getPassword())).thenReturn(false);

		// Execute the method (expecting BadRequestException)
		authenticationController.loginUser(userLoginDto, request);
	}

	@Test
	public void testGenerateToken_ValidToken() {
		// Prepare test data
		String token = "valid_token";
		String userEmail = "test@example.com";
		Users user = new Users();
		String domain = "valid_Domain";
		String newToken = "new_token";

		// Mock dependencies
		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		when(utility.getTokenFromRequest(request)).thenReturn(token);
		when(utility.getDomain(request)).thenReturn(domain);
		when(jwtTokenHelper.getUsernameFromToken(token)).thenReturn(userEmail);
		when(usersService.findUserByEmail(userEmail)).thenReturn(user);
		doNothing().when(blackListedTokensService).invalidateToken(token);
		when(jwtTokenHelper.generateToken(user, domain)).thenReturn(newToken);

		// Execute the method
		ResponseEntity<JwtTokenResponse> response = authenticationController.generateToken(request);

		// Verify the response
		assertEquals(HttpStatus.OK, response.getStatusCode());
		JwtTokenResponse tokenResponse = response.getBody();
		assertNotNull(tokenResponse);
		assertEquals(newToken, tokenResponse.getToken());
	}

	@Test
	public void testValidateToken_ValidToken() {
		// prepareTest data
		String token = "valid_token";

		// Mock dependencies
		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		when(utility.getTokenFromRequest(request)).thenReturn(token);
		when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

		// Perform the request
		ResponseEntity<JwtTokenResponse> response = authenticationController.validateToken(request);

		// Verify the response
		assertEquals(HttpStatus.OK, response.getStatusCode());
		JwtTokenResponse tokenResponse = response.getBody();
		assertNotNull(tokenResponse);
		assertEquals(token, tokenResponse.getToken());
		assertEquals("Token is valid", tokenResponse.getMessage());
	}

}