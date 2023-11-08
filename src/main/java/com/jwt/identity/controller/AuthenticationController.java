package com.jwt.identity.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jwt.identity.aspects.exceptions.BadRequestException;
import com.jwt.identity.aspects.exceptions.NotFoundException;
import com.jwt.identity.dto.JwtTokenResponse;
import com.jwt.identity.dto.UserResponseDto;
import com.jwt.identity.dto.UsersLoginDto;
import com.jwt.identity.entity.Users;
import com.jwt.identity.service.BlackListedTokensService;
import com.jwt.identity.service.UsersService;
import com.jwt.identity.util.JwtTokenHelper;
import com.jwt.identity.util.PasswordService;
import com.jwt.identity.util.Utility;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api/v1/users")
public class AuthenticationController {

	@Autowired
	private UsersService usersService;

	@Autowired
	private JwtTokenHelper jwtTokenHelper;

	@Autowired
	private BlackListedTokensService blackListedTokensService;

	@Autowired
	private PasswordService passwordEncoder;
	
	@Autowired
	private Utility utility;




	@PostMapping("/authenticate")
	public ResponseEntity<JwtTokenResponse> loginUser(@RequestBody UsersLoginDto userLoginObj,
			HttpServletRequest request) {
		String domain = utility.getDomain(request);
		Users user = usersService.findUserByEmail(userLoginObj.getEmail());
		if (user == null) {
			throw new NotFoundException("User Not Found");
		}
		if (passwordEncoder.matches(userLoginObj.getPassword(), user.getPassword())) {
			String token = jwtTokenHelper.generateToken(user, domain);
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
			response.setToken(token);
			return ResponseEntity.ok(response);
		} else {
			throw new BadRequestException("Invalid Credentials");
		}
	}

	@Operation(summary = "Generate Token", description = "Generate Bearer Token")
	@SecurityRequirement(name = "Bearer Authentication")
	@GetMapping("/generateToken")
	public ResponseEntity<JwtTokenResponse> generateToken(HttpServletRequest request) {
		String token = utility.getTokenFromRequest(request);
		String domain = utility.getDomain(request);
		String userEmail = jwtTokenHelper.getUsernameFromToken(token);
		Users user = usersService.findUserByEmail(userEmail);
		blackListedTokensService.invalidateToken(token);
		String newToken = jwtTokenHelper.generateToken(user, domain);
		JwtTokenResponse response = new JwtTokenResponse();
		response.setToken(newToken);
		return ResponseEntity.ok(response);
	}

	@Operation(summary = "Validate Token", description = "Bearer Token Validation")
	@SecurityRequirement(name = "Bearer Authentication")
	@GetMapping("/validateToken")
	public ResponseEntity<JwtTokenResponse> validateToken(HttpServletRequest request) {
		JwtTokenResponse response = new JwtTokenResponse();
		String token = utility.getTokenFromRequest(request);
		response.setToken(token);
		response.setMessage("Token is valid");
		return ResponseEntity.ok(response);
	}
	
}
