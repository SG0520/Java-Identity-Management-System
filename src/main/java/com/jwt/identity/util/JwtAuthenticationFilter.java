package com.jwt.identity.util;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.jwt.identity.entity.UserRole;
import com.jwt.identity.entity.Users;
import com.jwt.identity.service.UsersService;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

	@Autowired
	private UserDetailsService userDetailsService;

	@Autowired
	private UsersService userService;

	@Autowired
	private JwtTokenHelper jwtToken;

	@Autowired
	private RequestMappingHandlerMapping handlerMapping;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		String requestToken = request.getHeader("Authorization");

		// Enable for local, dev and uat
		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Methods", "*");
		response.setHeader("Access-Control-Allow-Headers", "*");

		String username = null;
		String token = null;
		String contentType = "application/json";
		if (isPermittedURL(request)) {
			// Proceed with the request without authentication
			filterChain.doFilter(request, response);
			return;
		}

		if (requestToken == null) {
			logger.error("Empty JWT token.");
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.setContentType(contentType);
			response.getWriter().write("Empty JWT token");
			return;
		}
		if (requestToken.startsWith("Bearer ")) {
			token = requestToken.substring(7);
			try {
				username = this.jwtToken.getUsernameFromToken(token);
			} catch (IllegalArgumentException ex) {
				logger.error("Unable to get username from token.", ex);
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				response.setContentType(contentType);
				response.getWriter().write("Unable to get username from token");
			} catch (ExpiredJwtException ex) {
				logger.error("JWT token has expired.", ex);
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				response.setContentType(contentType);
				response.getWriter().write("JWT token has expired");
				return;
			} catch (MalformedJwtException ex) {
				logger.error("Malformed JWT token.", ex);
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				response.setContentType(contentType);
				response.getWriter().write("Malformed JWT Token");
				return;
			} catch (SignatureException ex) {
				logger.error("Invalid JWT signature.", ex);
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				response.setContentType(contentType);
				response.getWriter().write("Invalid JWT signature");
				return;
			}
		} else {
			logger.debug("JWT token doesn't begin with 'Bearer'");
		}

		// Once we get the token, now validate
		if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
			UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
			Users userObj = this.userService.findUserByEmail(username);
			if (Boolean.TRUE.equals(this.jwtToken.validateToken(token, userObj))) {
				HandlerMethod handlerMethod;
				try {
					handlerMethod = (HandlerMethod) handlerMapping.getHandler(request).getHandler();
					PreAuthorize preAuthorizeAnnotation = handlerMethod.getMethodAnnotation(PreAuthorize.class);
					if (preAuthorizeAnnotation != null && (!isUserAuthorized(userObj))) {
						// User is not authorized
						response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
						response.setContentType("application/json");
						response.getWriter().write("Unauthorized");
						return;
					}
				} catch (Exception ex) {
					logger.error("Error occurred while retrieving the handler method.", ex);
					response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
					response.setContentType(contentType);
					response.getWriter().write("Internal Server Error");
					return;
				}
				// Everything is going correctly
				// Now set authentication
				UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
						userDetails.getUsername(), userDetails.getPassword(), userDetails.getAuthorities());
				usernamePasswordAuthenticationToken
						.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
				SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
			} else {
				logger.debug("Invalid JWT Token or Blacklisted Token");
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				response.setContentType(contentType);
				response.getWriter().write("Invalid JWT Token or Blacklisted Token");
				return;
			}
		} else {
			logger.debug("Username is null or context is not null");
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.setContentType(contentType);
			response.getWriter().write("Username is null or context is not null");
			return;
		}

		filterChain.doFilter(request, response);
	}

	private boolean isPermittedURL(HttpServletRequest request) {
		String url = request.getRequestURI();
		String contextPath=request.getContextPath();
		 url= url.substring(contextPath.length());
		return url.matches("^/api/v1/users/authenticate$") || url.matches("^/api/v1/users$")
				|| url.matches("^/api/v1/forgotPassword$") || url.matches("^/api/v1/resetPassword$")
				|| url.matches("^/v3/api-docs/swagger-config$") || url.matches("^/swagger-ui/.*$")
				|| url.matches("^/swagger-ui.html$") || url.matches("^/swagger-resources/.*$")
				|| url.matches("^/v3/api-docs$") || url.matches("^/api/v1/userByPasswordToken$");
	}


	private boolean isUserAuthorized(Users userObj) {
		UserRole userTypeObj = userObj.getUserRole();
		String roleId = userTypeObj.getUserRoleId();
		return roleId.equals("Admin");
	}
	
}
