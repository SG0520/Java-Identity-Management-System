package com.jwt.identity.util;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.jwt.identity.entity.Tenant;
import com.jwt.identity.entity.Users;
import com.jwt.identity.repository.TenantRepository;
import com.jwt.identity.service.BlackListedTokensService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Component
public class JwtTokenHelper implements Serializable {

	private static final long serialVersionUID = -5690420608173508160L;

	public static final long JWT_TOKEN_VALIDITY = 24 * 60 * 60;

	@Value("${jwt.secret}")
	private String secret;

	@Autowired
	private TenantRepository tenantRepository;

	@Autowired
	private BlackListedTokensService blackListedTokensService;

	public String getUsernameFromToken(String token) {
		return getClaimFromToken(token, Claims::getSubject);
	}

	public Date getExpirationDateFromToken(String token) {
		return getClaimFromToken(token, Claims::getExpiration);
	}

	public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
		final Claims claims = getAllClaimsFromToken(token);
		return claimsResolver.apply(claims);
	}

	private Claims getAllClaimsFromToken(String token) {
		return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
	}

	public Boolean isTokenExpired(String token) {
		final Date expiration = getExpirationDateFromToken(token);
		return expiration.before(new Date());
	}

	public String generateToken(Users u,String domain) {
		Claims claims = Jwts.claims().setSubject(u.getEmail());
		claims.put("email", u.getEmail());
		claims.put("userRole", u.getUserRole().getUserRoleId());
		claims.put("userGuid", u.getUserGuid());
		claims.put("tenantId", u.getTenantId());
		Tenant tenantObj = tenantRepository.findById(u.getTenantId()).orElse(null);
		claims.put("tenantName", tenantObj.getTenantName());
		claims.put("firstName", u.getFirstName());
		claims.put("lastName", u.getLastName());
		claims.put("domain", domain);
		claims.put("jti", UUID.randomUUID().toString());
		claims.put("createdAt", new Date());
		return doGenerateToken(claims, u.getEmail());
	}

	private String doGenerateToken(Claims claims, String subject) {

		return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY * 1000))
				.signWith(SignatureAlgorithm.HS512, secret).compact();
	}

	public Boolean validateToken(String token, Users user) {
		final String username = getUsernameFromToken(token);
		return (username.equals(user.getEmail()) && !isTokenExpired(token)
				&& !this.blackListedTokensService.isTokenInvalidated(token));
	}

}
