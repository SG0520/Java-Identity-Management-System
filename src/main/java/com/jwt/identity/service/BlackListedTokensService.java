package com.jwt.identity.service;

import org.springframework.stereotype.Service;

@Service
public interface BlackListedTokensService {

	public void invalidateToken(String token);

	public boolean isTokenInvalidated(String token);

}
   