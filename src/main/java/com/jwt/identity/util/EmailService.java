package com.jwt.identity.util;

import java.net.URI;
import java.net.URISyntaxException;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EmailService {
	@Autowired
	private JavaMailSender javaMailSender;

	@Value("${from.email.address}")
	private String fromEmailAddress;

	public void sendPasswordResetEmail(String email, @NotNull String passwordToken, HttpServletRequest request)
			throws MessagingException {
		MimeMessage message = javaMailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message, true);
		helper.setFrom(fromEmailAddress);
		helper.setTo(email);
		helper.setSubject("Password Reset Link");
		String url = request.getRequestURL().toString();
		
		try {
			URI uri = new URI(url);
			String domain = uri.getHost();
			int port = uri.getPort();
			helper.setText((url.startsWith("https")?"https":"http")+"://" + domain + ":" + port + "/resetPassword?passwordToken=" + passwordToken, true);
			javaMailSender.send(message);
		} catch (URISyntaxException e) {
			log.error("Invalid URL: " + url);

		}
	}

}
