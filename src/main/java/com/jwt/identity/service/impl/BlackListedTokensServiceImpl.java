package com.jwt.identity.service.impl;

import java.time.Instant;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.jwt.identity.service.BlackListedTokensService;
import com.jwt.identity.util.JwtTokenHelper;

@Service
public class BlackListedTokensServiceImpl implements BlackListedTokensService {

	private static final String BLACKLISTED_TOKENS_KEY = "blacklisted_tokens";

	@Autowired
	private JwtTokenHelper jwtTokenHelper;

	@Autowired
	private RedisTemplate<String, String> redisTemplate;
//	public BlackListedTokensServiceImpl(RedisTemplate<String, String> redisTemplate) {
//		this.redisTemplate = redisTemplate;
//	}

	@Override
	public void invalidateToken(String token) {
		redisTemplate.opsForSet().add(BLACKLISTED_TOKENS_KEY, token);
		Date tokenExpirationDate = jwtTokenHelper.getExpirationDateFromToken(token);
		long timeToLiveMillis = tokenExpirationDate.getTime() - Instant.now().toEpochMilli();
		if (timeToLiveMillis > 0) {
			redisTemplate.expire(token, timeToLiveMillis, TimeUnit.MILLISECONDS);
		}
	}

	@Override
	public boolean isTokenInvalidated(String token) {
		return redisTemplate.opsForSet().isMember(BLACKLISTED_TOKENS_KEY, token);
	}

	public void deleteExpiredTokens() {
		Set<String> blacklistedTokens = redisTemplate.opsForSet().members(BLACKLISTED_TOKENS_KEY);
		for (String token : blacklistedTokens) {
			if (jwtTokenHelper.isTokenExpired(token)) {
				redisTemplate.opsForSet().remove(BLACKLISTED_TOKENS_KEY, token);
			}
		}
	}
}
