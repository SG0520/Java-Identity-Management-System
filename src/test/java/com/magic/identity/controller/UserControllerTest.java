package com.jwt.identity.controller;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.multipart.MultipartFile;

import com.jwt.identity.aspects.exceptions.BadRequestException;
import com.jwt.identity.aspects.exceptions.ConflictException;
import com.jwt.identity.aspects.exceptions.UnauthorizedException;
import com.jwt.identity.dto.EmailRequestDto;
import com.jwt.identity.dto.JwtTokenResponse;
import com.jwt.identity.dto.PasswordChangeRequest;
import com.jwt.identity.dto.PasswordResetRequest;
import com.jwt.identity.dto.ResetPasswordResponse;
import com.jwt.identity.dto.ResetPasswordUserDetailsDto;
import com.jwt.identity.dto.UserDto;
import com.jwt.identity.dto.UserResponseDto;
import com.jwt.identity.entity.ForgotPasswordToken;
import com.jwt.identity.entity.UserRole;
import com.jwt.identity.entity.Users;
import com.jwt.identity.service.BlackListedTokensService;
import com.jwt.identity.service.ForgotPasswordTokenService;
import com.jwt.identity.service.UsersService;
import com.jwt.identity.util.EmailService;
import com.jwt.identity.util.JwtTokenHelper;
import com.jwt.identity.util.PasswordService;
import com.jwt.identity.util.Utility;
import com.jwt.identity.util.XlsxHelper;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

public class UserControllerTest {
	@Mock
	private UsersService usersService;

	@Mock
	private BlackListedTokensService blackListedTokensService;

	@Mock
	private JwtTokenHelper jwtTokenHelper;

	@InjectMocks
	private UserController userController;

	@Mock
	private XlsxHelper xlsxHelper;

	@Mock
	private ForgotPasswordTokenService forgotPasswordTokenService;

	@Mock
	private EmailService emailService;

	@Mock
	private UserDetailsService userDetailsService;

	@Mock
	private PasswordService passwordEncoder;

	@Mock
	private Utility utility;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	private MultipartFile createMockMultipartFile() throws IOException {
		String content = "user1@example.com,John,Doe\nuser2@example.com,Jane,Smith";
		return new MockMultipartFile("file", "test.xlsx",
				"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", content.getBytes());
	}

	//@Test
	public void testCreateUser_SuccessfulCreation() {
		// prepare test data
		UserDto userDto = new UserDto();
		userDto.setEmail("test@example.com");
		userDto.setTenantId(712);
		Users newUser = new Users();
		UserRole userRole = new UserRole();
		userRole.setUserRoleId("role_id");
		newUser.setUserRole(userRole);
		newUser.setEmail("shriyamgupta@gmail.com");
		newUser.setTenantId(712);

		// Mock dependencies
		when(usersService.findUserByEmailAndTenantId("shriyamgupta@gmail.com", 712)).thenReturn(null);
		when(usersService.createUser(userDto)).thenReturn(newUser);

		// Perform the request
		ResponseEntity<Object> response = userController.createUser(userDto);

		// Verify the response
		@SuppressWarnings("unchecked")
		Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
		UserResponseDto userResponseDto = (UserResponseDto) responseBody.get("User Data");
		assertEquals("User created successfully.", responseBody.get("Status"));
		assertEquals("shriyamgupta@gmail.com", userResponseDto.getEmail());
		assertEquals(HttpStatus.OK, response.getStatusCode());
	}

	@Test
	public void testCreateUser_InvalidEmail() {
		// prepare test data
		UserDto userDto = new UserDto();
		userDto.setTenantId(712);
		userDto.setEmail("invalid_email");

		// Perform the request
		ResponseEntity<Object> response = userController.createUser(userDto);

		// Verify the response
		@SuppressWarnings("unchecked")
		Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertEquals("Invalid Email!", responseBody.get("Error"));
	}

	@Test(expected = ConflictException.class)
	public void testCreateUser_UserAlreadyExists() {
		// prepare test data
		UserDto userDto = new UserDto();
		userDto.setEmail("shriyamgupta@gmail.com");
		userDto.setTenantId(712);
		Users existingUser = new Users();

		// Perform the request
		when(usersService.findUserByEmailAndTenantId("shriyamgupta@gmail.com", 712)).thenReturn(existingUser);
		userController.createUser(userDto);
	}

	@Test
	public void testUploadFile_SuccessfulUpload() throws Exception {
		// prepare test data
		Claims claims = Jwts.claims();
		claims.put("tenantId", 712);
		Integer tenantId = 712;
		String email = "shriyamgupta@gmail.com";
		Users supUser = new Users();
		supUser.setTenantId(tenantId);
		String token = "valid_token";

		// Mock dependencies
		MultipartFile file = createMockMultipartFile(); // Create a mock MultipartFile object
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(utility.getTokenFromRequest(request)).thenReturn(token);
		when(jwtTokenHelper.getClaimFromToken("token", claimsObj -> claims.get("tenantId"))).thenReturn(tenantId);
		when(jwtTokenHelper.getUsernameFromToken("token")).thenReturn(email);
		when(usersService.findUserByEmail(email)).thenReturn(supUser);
		when(xlsxHelper.hasXlsxFormat(file)).thenReturn(true);
		doNothing().when(usersService).registerBulkUser(file, supUser);

		// Perform the request
		ResponseEntity<Object> response = userController.uploadFile(request, file);

		// Verify the response
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals("Uploaded the file successfully: " + file.getOriginalFilename(), response.getBody());
	}

	@Test
	public void testUploadFile_InvalidFormat() throws Exception {
		// prepare test data
		Claims claims = Jwts.claims();
		claims.put("tenantId", 712);
		Integer tenantId = 712;
		String email = "shriyamgupta@gmail.com";
		Users supUser = new Users();
		String token = "valid_token";

		// Mock dependencies
		MultipartFile file = createMockMultipartFile();
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(utility.getTokenFromRequest(request)).thenReturn(token);
		when(jwtTokenHelper.getClaimFromToken("token", claimsObj -> claims.get("tenantId"))).thenReturn(tenantId);
		when(jwtTokenHelper.getUsernameFromToken("token")).thenReturn(email);
		when(usersService.findUserByEmail(email)).thenReturn(supUser);
		when(xlsxHelper.hasXlsxFormat(file)).thenReturn(false);

		// Perform the request
		ResponseEntity<Object> response = userController.uploadFile(request, file);

		// Verify the response
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertEquals("Please upload a Xlsx file!", response.getBody());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testDeleteUser_Success() {
		String userGuid = "user-guid";
		doNothing().when(usersService).deleteUserById(userGuid);
		ResponseEntity<Object> response = userController.deleteUser(userGuid);
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals("User deleted successfully.", ((Map<String, Object>) response.getBody()).get("Status"));
	}

	@Test
	public void testForgotPassword_UserFound_Success() throws MessagingException {
		// prepare test data
		Users user = new Users();
		user.setEmail("shriyamgupta@gmail.com");
		EmailRequestDto emailRequest = new EmailRequestDto();
		emailRequest.setEmail(user.getEmail());

		// Mock dependencies
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(usersService.findUserByEmail(user.getEmail())).thenReturn(user);
		ForgotPasswordToken passwordToken = new ForgotPasswordToken();
		passwordToken.setPasswordToken("password_token");
		when(forgotPasswordTokenService.createPasswordToken(user)).thenReturn(passwordToken);

		// Perform the request
		ResponseEntity<JwtTokenResponse> response = userController.forgotPassword(emailRequest, request);

		// Verify the response
		assertEquals(HttpStatus.OK, response.getStatusCode());
		JwtTokenResponse jwtResponse = response.getBody();
		assertNotNull(jwtResponse);
		assertEquals("password_token", jwtResponse.getToken());
		assertEquals("Password reset Link Send to Email", jwtResponse.getMessage());
		verify(emailService).sendPasswordResetEmail(eq(user.getEmail()), eq("password_token"),
				any(HttpServletRequest.class));
	}

	@Test
	public void testForgotPassword_UserNotFound() throws MessagingException {
		// prepare test data
		String email = "shriyamgupta@gmail.com";
		EmailRequestDto emailRequest = new EmailRequestDto();
		emailRequest.setEmail(email);

		// Mocking the UsersService to return null (user not found)
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(usersService.findUserByEmail(email)).thenReturn(null);

		// Perform the request
		ResponseEntity<JwtTokenResponse> response = userController.forgotPassword(emailRequest, request);

		// Verify the response
		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
		JwtTokenResponse jwtResponse = response.getBody();
		assertNotNull(jwtResponse);
		assertNull(jwtResponse.getToken());
		assertNull(jwtResponse.getMessage());

		// Verify the email service was not called
		verify(emailService, never()).sendPasswordResetEmail(anyString(), anyString(), any(HttpServletRequest.class));
	}

	@Test
	public void testForgotPassword_EmailSendingFailed_ExpectationFailedStatus() throws MessagingException {
		// Preparing Test Data
		Users user = new Users();
		user.setEmail("shriyamgupta@gmail.com");
		EmailRequestDto emailRequest = new EmailRequestDto();
		emailRequest.setEmail(user.getEmail());

		// Mocking Dependencies
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(usersService.findUserByEmail(user.getEmail())).thenReturn(user);
		ForgotPasswordToken passwordToken = new ForgotPasswordToken();
		passwordToken.setPasswordToken("password_token");
		when(forgotPasswordTokenService.createPasswordToken(user)).thenReturn(passwordToken);
		doThrow(new MessagingException()).when(emailService).sendPasswordResetEmail(eq(user.getEmail()),
				eq("password_token"), any(HttpServletRequest.class));

		// Perform the request
		ResponseEntity<JwtTokenResponse> response = userController.forgotPassword(emailRequest, request);

		// Verify the response
		assertEquals(HttpStatus.EXPECTATION_FAILED, response.getStatusCode());
		JwtTokenResponse jwtResponse = response.getBody();
		assertNotNull(jwtResponse);
		assertNull(jwtResponse.getToken());
		assertEquals("Failed to send message", jwtResponse.getMessage());
	}

	@Test
	public void testResetPassword_ValidToken_PasswordChangedSuccessfully() {
		// Preparing Test Data
		PasswordResetRequest passwordResetRequest = new PasswordResetRequest();
		passwordResetRequest.setNewPassword("new_password");
		passwordResetRequest.setReNewPassword("new_password");
		String passwordToken = "valid_token";
		Users user = new Users();
		user.setEmail("shriyamgupta99@gmail.com");
		user.setFirstName("shriyam");
		user.setLastName("gupta");
		ForgotPasswordToken passwordTokenObj = new ForgotPasswordToken();
		passwordTokenObj.setEmail("shriyamgupta99@gmail.com");
		passwordTokenObj.setExpiryDate(new Date(System.currentTimeMillis() + 3600000));
		passwordTokenObj.setPasswordToken("valid_token");

		// Mocking the ForgotPasswordTokenService to return a password token
		when(forgotPasswordTokenService.getByPasswordToken(passwordToken)).thenReturn(passwordTokenObj);
		when(usersService.findUserByEmail("shriyamgupta99@gmail.com")).thenReturn(user);

		// Perform the request
		ResponseEntity<ResetPasswordResponse> response = userController.resetPassword(passwordResetRequest,
				passwordToken);

		// Verify the response
		assertEquals(HttpStatus.OK, response.getStatusCode());
		ResetPasswordResponse resetPasswordResponse = response.getBody();
		assertNotNull(resetPasswordResponse);
		assertEquals("Password Changed Sucessfully", resetPasswordResponse.getMessage());
		ResetPasswordUserDetailsDto resetPasswordUserDetailsDto = resetPasswordResponse.getResetPasswordUserDetails();
		assertNotNull(resetPasswordUserDetailsDto);
		assertEquals("shriyamgupta99@gmail.com", resetPasswordUserDetailsDto.getEmail());
		assertEquals("shriyam", resetPasswordUserDetailsDto.getFirstName());
		assertEquals("gupta", resetPasswordUserDetailsDto.getLastName());
		verify(forgotPasswordTokenService).deletePasswordToken("valid_token");
		verify(usersService).changePassword(user);
	}

	@Test(expected = BadRequestException.class)
	public void testResetPassword_WhenTokenIsBlank_BadRequestStatus() {
		// Mocking the password reset request and password token
		PasswordResetRequest passwordResetRequest = new PasswordResetRequest();
		passwordResetRequest.setNewPassword("new_password");
		passwordResetRequest.setReNewPassword("new_password");
		String passwordToken = "";

		// Perform the request
		userController.resetPassword(passwordResetRequest, passwordToken);
	}

	@Test(expected = BadRequestException.class)
	public void testResetPassword_WhenTokenIsNull_BadRequestStatus() {
		// Mocking the password reset request and password token
		PasswordResetRequest passwordResetRequest = new PasswordResetRequest();
		passwordResetRequest.setNewPassword("new_password");
		passwordResetRequest.setReNewPassword("new_password");
		String passwordToken = "invalid_token";

		// Mocking the ForgotPasswordTokenService to return null (invalid token)
		when(forgotPasswordTokenService.getByPasswordToken(passwordToken)).thenReturn(null);

		// Perform the request
		userController.resetPassword(passwordResetRequest, passwordToken);
	}

	@Test(expected = UnauthorizedException.class)
	public void testResetPassword_ExpiredToken_UnauthorizedStatus() {
		// Mocking the password reset request and password token
		PasswordResetRequest passwordResetRequest = new PasswordResetRequest();
		passwordResetRequest.setNewPassword("new_password");
		passwordResetRequest.setReNewPassword("new_password");

		String passwordToken = "expired_token";

		// Mocking the ForgotPasswordTokenService to return an expired password token
		ForgotPasswordToken passwordTokenObj = new ForgotPasswordToken();
		passwordTokenObj.setEmail("test@example.com");
		passwordTokenObj.setExpiryDate(new Date(System.currentTimeMillis() - 3600000)); // Set expiry date 1 hour in the
																						// past
		when(forgotPasswordTokenService.getByPasswordToken(passwordToken)).thenReturn(passwordTokenObj);

		// Perform the request
		userController.resetPassword(passwordResetRequest, passwordToken);

	}

	@Test
	public void testResetPassword_PasswordMismatch_Success() {
		// Mocking the password reset request and password token
		PasswordResetRequest passwordResetRequest = new PasswordResetRequest();
		passwordResetRequest.setNewPassword("new_password");
		passwordResetRequest.setReNewPassword("wrong_password");
		ForgotPasswordToken passwordTokenObj = new ForgotPasswordToken();
		passwordTokenObj.setEmail("shriyamgupta99@gmail.com");
		passwordTokenObj.setExpiryDate(new Date(System.currentTimeMillis() + 3600000));
		passwordTokenObj.setPasswordToken("valid_token");
		String passwordToken = "valid_token";

		// Mock Dependencies
		when(forgotPasswordTokenService.getByPasswordToken(passwordToken)).thenReturn(passwordTokenObj);

		// Perform the request
		ResponseEntity<ResetPasswordResponse> response = userController.resetPassword(passwordResetRequest,
				passwordToken);

		// Verify the response
		assertEquals(HttpStatus.OK, response.getStatusCode());
		ResetPasswordResponse resetPasswordResponse = response.getBody();
		assertNotNull(resetPasswordResponse);
		assertEquals("Password and Re-entered password doesn't match", resetPasswordResponse.getMessage());

		// Verify the ForgotPasswordTokenService was not called to delete the token
		verify(forgotPasswordTokenService, never()).deletePasswordToken(anyString());

		// Verify the UsersService was not called to change the password
		verify(usersService, never()).changePassword(any(Users.class));
	}

	@Test
	public void testLogout_ValidToken_SuccessfulLogout() {
		// Mocking the HttpServletRequest and utility
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(utility.getTokenFromRequest(request)).thenReturn("valid_token");

		// Perform the request
		ResponseEntity<String> response = userController.logout(request);

		// Verify the response
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals("Sucessfully Logged Out", response.getBody());

		// Verify the BlackListedTokensService was called to invalidate the token
		verify(blackListedTokensService).invalidateToken("valid_token");
	}

	@Test
	public void testGetUserDetails_ValidToken_ReturnsUserDetails() {
		// Preparing Test Data
		Users user = new Users();
		user.setEmail("shriyamgupta99@gmail.com");
		user.setFirstName("Shriyam");
		user.setLastName("Gupta");
		UserRole userRole = new UserRole();
		userRole.setUserRoleId("role_id");
		user.setUserRole(userRole);
		user.setGender("Male");
		user.setTelephone("8529486268");
		user.setTenantId(712);
		user.setTitle("None");

		// ... set other properties of the user
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(utility.getTokenFromRequest(request)).thenReturn("valid_token");
		when(jwtTokenHelper.getUsernameFromToken("valid_token")).thenReturn("shriyamgupta99@gmail.com");
		when(usersService.findUserByEmail("shriyamgupta99@gmail.com")).thenReturn(user);

		// Perform the request
		ResponseEntity<JwtTokenResponse> response = userController.getUserDetails(request);

		// Verify the response
		assertEquals(HttpStatus.OK, response.getStatusCode());

		JwtTokenResponse tokenResponse = response.getBody();
		assertNotNull(tokenResponse);
		UserResponseDto userResponseDto = tokenResponse.getUser();
		assertNotNull(userResponseDto);
		assertEquals("shriyamgupta99@gmail.com", userResponseDto.getEmail());
		assertEquals("Shriyam", userResponseDto.getFirstName());
		assertEquals("Gupta", userResponseDto.getLastName());

		// Verify that the mocked dependencies were called correctly
		verify(utility).getTokenFromRequest(request);
		verify(jwtTokenHelper).getUsernameFromToken("valid_token");
		verify(usersService).findUserByEmail("shriyamgupta99@gmail.com");
	}

	@Test
	public void testGetUserByPasswordToken_ValidToken_ReturnsUser() {
		// Mocking the ForgotPasswordTokenService
		ForgotPasswordToken passwordTokenObj = new ForgotPasswordToken();
		passwordTokenObj.setPasswordToken("valid_token");
		passwordTokenObj.setExpiryDate(new Date(System.currentTimeMillis() + 1000));

		// Mocking Dependency
		when(forgotPasswordTokenService.getByPasswordToken("valid_token")).thenReturn(passwordTokenObj);

		// Perform the request
		ResponseEntity<ForgotPasswordToken> response = userController.getUserByPasswordToken("valid_token");

		// Verify the response
		assertEquals(HttpStatus.OK, response.getStatusCode());
		ForgotPasswordToken tokenResponse = response.getBody();
		assertNotNull(tokenResponse);
		assertEquals("valid_token", tokenResponse.getPasswordToken());

		// Verify that the mocked dependency was called correctly
		verify(forgotPasswordTokenService).getByPasswordToken("valid_token");
	}

	@Test(expected = BadRequestException.class)
	public void testGetUserByPasswordToken_BlankToken_ThrowsBadRequestException() {
		// Perform the request with a blank token
		userController.getUserByPasswordToken("");

	}

	@Test(expected = BadRequestException.class)
	public void testGetUserByPasswordToken_InvalidToken_ThrowsBadRequestException() {

		// Mocking the ForgotPasswordTokenService to return null for an invalid token
		when(forgotPasswordTokenService.getByPasswordToken("invalid_token")).thenReturn(null);

		// Perform the request with an invalid token
		userController.getUserByPasswordToken("invalid_token");

		// Verify that the mocked dependency was called correctly
		verify(forgotPasswordTokenService).getByPasswordToken("invalid_token");
	}

	@Test(expected = UnauthorizedException.class)
	public void testGetUserByPasswordToken_ExpiredToken_ThrowsUnauthorizedException() {
		// Mocking the ForgotPasswordTokenService to return an expired token
		ForgotPasswordToken passwordTokenObj = new ForgotPasswordToken();
		passwordTokenObj.setExpiryDate(new Date(System.currentTimeMillis() - 1000)); // Set the expiry date to a past
																						// date
		when(forgotPasswordTokenService.getByPasswordToken("expired_token")).thenReturn(passwordTokenObj);

		// Perform the request with an expired token
		userController.getUserByPasswordToken("expired_token");

		// Verify that the mocked dependency was called correctly
		verify(forgotPasswordTokenService).getByPasswordToken("expired_token");
	}

	@Test
	public void testChangePassword_ValidRequest_PasswordChangedSuccessfully() {
		// Mocking the UsersService
		Users userObj = new Users();
		userObj.setEmail("shriyamgupta@gmail.com");
		userObj.setPassword("old_password");
		PasswordChangeRequest passwordChangeRequest = new PasswordChangeRequest();
		passwordChangeRequest.setOldPassword("old_password");
		passwordChangeRequest.setNewPassword("new_password");
		passwordChangeRequest.setReNewPassword("new_password");

		// Mocking Dependencies
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(usersService.findUserByEmail("shriyamgupta@gmail.com")).thenReturn(userObj);
		when(utility.getTokenFromRequest(request)).thenReturn("valid_token");
		when(jwtTokenHelper.getUsernameFromToken("valid_token")).thenReturn("shriyamgupta@gmail.com");
		when(passwordEncoder.matches(passwordChangeRequest.getOldPassword(), userObj.getPassword())).thenReturn(true);

		// Perform the request

		ResponseEntity<String> response = userController.changePassword(passwordChangeRequest, request);

		// Verify the response
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals("Password Changed Successfully", response.getBody());

		// Verify that the user's password was changed
		verify(usersService).changePassword(userObj);
		assertEquals("new_password", userObj.getPassword());
	}

	@Test(expected = BadRequestException.class)
	public void testChangePassword_BlankOldPassword_ThrowsBadRequestException() {
		// Perform the request with a blank old password
		PasswordChangeRequest passwordChangeRequest = new PasswordChangeRequest();
		passwordChangeRequest.setOldPassword("");
		passwordChangeRequest.setNewPassword("new_password");
		passwordChangeRequest.setReNewPassword("new_password");
		userController.changePassword(passwordChangeRequest, mock(HttpServletRequest.class));

		// Expect BadRequestException to be thrown
	}

	@Test(expected = BadRequestException.class)
	public void testChangePassword_InvalidOldPassword_ThrowsBadRequestException() {
		// Mocking the UsersService to return a user with a different password
		Users userObj = new Users();
		userObj.setEmail("shriyamgupta@gmail.com");
		userObj.setPassword("old_password");
		PasswordChangeRequest passwordChangeRequest = new PasswordChangeRequest();
		passwordChangeRequest.setOldPassword("old_password");
		passwordChangeRequest.setNewPassword("new_password");
		passwordChangeRequest.setReNewPassword("new_password");

		// Mocked Dependencies
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(usersService.findUserByEmail("shriyamgupta@gmail.com")).thenReturn(userObj);
		when(utility.getTokenFromRequest(request)).thenReturn("valid_token");
		when(jwtTokenHelper.getUsernameFromToken("valid_token")).thenReturn("shriyamgupta@gmail.com");
		when(passwordEncoder.matches(passwordChangeRequest.getOldPassword(), userObj.getPassword())).thenReturn(false);

		// Perform the request with an invalid old password

		userController.changePassword(passwordChangeRequest, request);

		// Verify that the UsersService dependency was called correctly
		verify(usersService).findUserByEmail("shriyamgupta@gmail.com");
	}

	@Test(expected = BadRequestException.class)
	public void testChangePassword_PasswordMismatch_ThrowsBadRequestException() {
		// Perform the request with mismatched new passwords
		Users userObj = new Users();
		userObj.setEmail("shriyamgupta@gmail.com");
		userObj.setPassword("old_password");
		PasswordChangeRequest passwordChangeRequest = new PasswordChangeRequest();
		passwordChangeRequest.setOldPassword("old_password");
		passwordChangeRequest.setNewPassword("new_password");
		passwordChangeRequest.setReNewPassword("different_password");

		// Mocked Dependencies
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(usersService.findUserByEmail("shriyamgupta@gmail.com")).thenReturn(userObj);
		when(utility.getTokenFromRequest(request)).thenReturn("valid_token");
		when(jwtTokenHelper.getUsernameFromToken("valid_token")).thenReturn("shriyamgupta@gmail.com");
		when(passwordEncoder.matches(passwordChangeRequest.getOldPassword(), userObj.getPassword())).thenReturn(true);

		// Perform Request
		userController.changePassword(passwordChangeRequest, request);
	}
}
