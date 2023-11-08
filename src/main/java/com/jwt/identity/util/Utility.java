package com.jwt.identity.util;

import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
@Component
@Slf4j
public class Utility {
	public  String getDomain(HttpServletRequest request) {
		String url = request.getRequestURL().toString();
		try {
			URI uri = new URI(url);
			return uri.getHost();
		} catch (URISyntaxException e) {
			log.error("Invalid URL: " + url);
			return null;
		}
	}

	public String getTokenFromRequest(HttpServletRequest request) {
		String requestToken = request.getHeader("Authorization");
		return requestToken.substring(7);
	}

}
