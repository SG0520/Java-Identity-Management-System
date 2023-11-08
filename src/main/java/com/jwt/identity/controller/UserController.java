package com.jwt.identity.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.jwt.identity.aspects.exceptions.BadRequestException;
import com.jwt.identity.aspects.exceptions.ConflictException;
import com.jwt.identity.aspects.exceptions.NotFoundException;
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
import com.jwt.identity.entity.Users;
import com.jwt.identity.service.BlackListedTokensService;
import com.jwt.identity.service.ForgotPasswordTokenService;
import com.jwt.identity.service.UsersService;
import com.jwt.identity.util.EmailService;
import com.jwt.identity.util.JwtTokenHelper;
import com.jwt.identity.util.PasswordService;
import com.jwt.identity.util.Utility;
import com.jwt.identity.util.XlsxHelper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("api/v1")
public class UserController {

	@Autowired
	private UsersService usersService;

	@Autowired
	private JwtTokenHelper jwtTokenHelper;

	@Autowired
	private BlackListedTokensService blackListedTokensService;

	@Autowired
	private ForgotPasswordTokenService forgotPasswordTokenService;

	@Autowired
	private PasswordService passwordEncoder;

	@Autowired
	private EmailService emailService;

	@Autowired
	private XlsxHelper xlsxHelper;

	@Autowired
	private Utility utility;

	@PostMapping("/users")
	public ResponseEntity<Object> createUser(@RequestBody UserDto userDto) {
		Map<String, Object> response = new HashMap<>();
		if (!isEmailValid(userDto.getEmail())) {
			response.put("Error", "Invalid Email!");
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}
		Users user = usersService.findUserByEmailAndTenantId(userDto.getEmail(), userDto.getTenantId());
		if (user != null) {
			throw new ConflictException("User with same email and tenantId already exist");
		}
		Users newUser = usersService.createUser(userDto);
		UserResponseDto userResponseDtoObj = new UserResponseDto();
		userResponseDtoObj.setEmail(newUser.getEmail());
		userResponseDtoObj.setFirstName(newUser.getFirstName());
		userResponseDtoObj.setLastName(newUser.getLastName());
		userResponseDtoObj.setRoleId(newUser.getUserRole().getUserRoleId());
		userResponseDtoObj.setGender(newUser.getGender());
		userResponseDtoObj.setTelephone(newUser.getTelephone());
		userResponseDtoObj.setTenantId(newUser.getTenantId());
		userResponseDtoObj.setTitle(newUser.getTitle());
		response.put("Status", "User created successfully.");
		response.put("User Data", userResponseDtoObj);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	//@PreAuthorize("hasAuthority('ROLE_Admin')")
	@Operation(summary = "Bulk Upload Users", description = "Bulk Upload Users")
	@SecurityRequirement(name = "Bearer Authentication")
	@PostMapping(value = "/users/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<Object> uploadFile(HttpServletRequest request,
			@RequestPart("file") @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE, schema = @Schema(type = "string", format = "binary"))) MultipartFile file) {
		String message = "";
		System.out.println(file);
		String token = utility.getTokenFromRequest(request);
		String email = jwtTokenHelper.getUsernameFromToken(token);
		Users supUser = usersService.findUserByEmail(email);
		if (xlsxHelper.hasXlsxFormat(file)) {
			try {
				usersService.registerBulkUser(file, supUser);
				message = "Uploaded the file successfully: " + file.getOriginalFilename();
				return ResponseEntity.status(HttpStatus.OK).body(message);
			} catch (NotFoundException e) {
				message = e.getMessage() + "  " + "Upload From Sheet Failed";
				return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(message);
			}

			catch (Exception e) {
				message = "Could not upload the file: " + file.getOriginalFilename() + "!";
				return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(message);
			}
		}
		message = "Please upload a Xlsx file!";
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
	}

	@Operation(summary = "Delete User", description = "Delete User By Token")
	@SecurityRequirement(name = "Bearer Authentication")
	@DeleteMapping("/users/{userGuid}")
	public ResponseEntity<Object> deleteUser(@PathVariable String userGuid) {
		Map<String, String> response = new HashMap<>();
		// System.out.println("userGuid: " + userGuid);
		usersService.deleteUserById(userGuid);
		response.put("Status", "User deleted successfully.");
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	private boolean isEmailValid(String email) {
		return Pattern.matches("[_a-zA-Z1-9]+(\\.[A-Za-z0-9]*)*@[A-Za-z0-9]+\\.[A-Za-z0-9]+(\\.[A-Za-z0-9]*)*", email);
	}

	@PostMapping("/forgotPassword")
	public ResponseEntity<JwtTokenResponse> forgotPassword(@RequestBody EmailRequestDto emailRequest,
			HttpServletRequest request) {
		JwtTokenResponse jwtResponse = new JwtTokenResponse();
		String email = emailRequest.getEmail();
		Users user = usersService.findUserByEmail(email);
		if (user == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(jwtResponse);
		}
		ForgotPasswordToken passwordKey = forgotPasswordTokenService.createPasswordToken(user);
		try {
			emailService.sendPasswordResetEmail(user.getEmail(), passwordKey.getPasswordToken(), request);
		} catch (MessagingException e) {
			jwtResponse.setMessage("Failed to send message");
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(jwtResponse);
		}
		jwtResponse.setToken(passwordKey.getPasswordToken());
		jwtResponse.setMessage("Password reset Link Send to Email");
		return ResponseEntity.ok(jwtResponse);
	}

	@PostMapping("/resetPassword")
	public ResponseEntity<ResetPasswordResponse> resetPassword(@RequestBody PasswordResetRequest passwordResetRequest,
			@RequestParam String passwordToken) {
		ResetPasswordResponse resetPasswordResponse = new ResetPasswordResponse();
		ResetPasswordUserDetailsDto resetPasswordUserDetailsDto = new ResetPasswordUserDetailsDto();
		if (StringUtils.isBlank(passwordToken)) {
			throw new BadRequestException("Invalid Password-Token");
		}
		ForgotPasswordToken passwordTokenObj = forgotPasswordTokenService.getByPasswordToken(passwordToken);
		if (passwordTokenObj == null) {
			throw new BadRequestException("Invalid Password-Token");
		}
		if (new Date().after(passwordTokenObj.getExpiryDate())) {
			throw new UnauthorizedException("Password-Key Already Expired");
		}
		if (passwordResetRequest.getNewPassword().equalsIgnoreCase(passwordResetRequest.getReNewPassword())) {
			Users user = usersService.findUserByEmail(passwordTokenObj.getEmail());
			user.setPassword(passwordResetRequest.getNewPassword());
			usersService.changePassword(user);
			resetPasswordUserDetailsDto.setEmail(user.getEmail());
			resetPasswordUserDetailsDto.setFirstName(user.getFirstName());
			resetPasswordUserDetailsDto.setLastName(user.getLastName());
			forgotPasswordTokenService.deletePasswordToken(passwordTokenObj.getPasswordToken());
			resetPasswordResponse.setMessage("Password Changed Sucessfully");
			resetPasswordResponse.setResetPasswordUserDetails(resetPasswordUserDetailsDto);
			return ResponseEntity.ok(resetPasswordResponse);
		}
		resetPasswordResponse.setMessage("Password and Re-entered password doesn't match");
		return ResponseEntity.ok(resetPasswordResponse);
	}

	@Operation(summary = "Logout", description = "Logout User")
	@SecurityRequirement(name = "Bearer Authentication")
	@PostMapping("/logout")
	public ResponseEntity<String> logout(HttpServletRequest request) {
		String token = utility.getTokenFromRequest(request);
		blackListedTokensService.invalidateToken(token);
		return ResponseEntity.ok("Sucessfully Logged Out");
	}

	@Operation(summary = "Get User Details", description = "Get User Details By User Token")
	@SecurityRequirement(name = "Bearer Authentication")
	@GetMapping("/userDetails")
	public ResponseEntity<JwtTokenResponse> getUserDetails(HttpServletRequest request) {
		String token = utility.getTokenFromRequest(request);
		String email = jwtTokenHelper.getUsernameFromToken(token);
		Users user = usersService.findUserByEmail(email);
		JwtTokenResponse response = new JwtTokenResponse();
		UserResponseDto userResponseDtoObj = new UserResponseDto();
		userResponseDtoObj.setEmail(user.getEmail());
		userResponseDtoObj.setFirstName(user.getFirstName());
		userResponseDtoObj.setLastName(user.getLastName());
		userResponseDtoObj.setRoleId(user.getUserRole().getUserRoleId());
		userResponseDtoObj.setGender(user.getGender());
		userResponseDtoObj.setTelephone(user.getTelephone());
		userResponseDtoObj.setTenantId(user.getTenantId());
		userResponseDtoObj.setTitle(user.getTitle());
		response.setUser(userResponseDtoObj);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/userByPasswordToken")
	public ResponseEntity<ForgotPasswordToken> getUserByPasswordToken(@RequestParam String passwordToken) {
		if (StringUtils.isBlank(passwordToken)) {
			throw new BadRequestException("Invalid Password-Token");
		}
		ForgotPasswordToken passwordTokenObj = forgotPasswordTokenService.getByPasswordToken(passwordToken);
		if (passwordTokenObj == null) {
			throw new BadRequestException("Invalid Password-Token");
		}
		if (new Date().after(passwordTokenObj.getExpiryDate())) {
			throw new UnauthorizedException("Password-Key Already Expired");
		}
		return ResponseEntity.ok(passwordTokenObj);
	}

	@Operation(summary = "Change Password", description = "Change Password by Token")
	@SecurityRequirement(name = "Bearer Authentication")
	@PostMapping("/changePassword")
	public ResponseEntity<String> changePassword(@RequestBody PasswordChangeRequest passwordChangeRequest,
			HttpServletRequest request) {
		if (StringUtils.isBlank(passwordChangeRequest.getOldPassword())) {
			throw new BadRequestException("Invalid Old password");
		}
		String token = utility.getTokenFromRequest(request);
		String email = jwtTokenHelper.getUsernameFromToken(token);
		Users userObj = usersService.findUserByEmail(email);
		if (!passwordEncoder.matches(passwordChangeRequest.getOldPassword(), userObj.getPassword())) {
			throw new BadRequestException("Invalid Old Password");
		}
		if (passwordChangeRequest.getNewPassword().equalsIgnoreCase(passwordChangeRequest.getReNewPassword())) {
			userObj.setPassword(passwordChangeRequest.getNewPassword());
			usersService.changePassword(userObj);
			return ResponseEntity.ok("Password Changed Successfully");
		}
		throw new BadRequestException("Password and Re entered password doesn't match");
	}

}
