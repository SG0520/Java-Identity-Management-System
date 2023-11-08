package com.jwt.identity.aspects;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.jwt.identity.aspects.exceptions.BadRequestException;
import com.jwt.identity.aspects.exceptions.ConflictException;
import com.jwt.identity.aspects.exceptions.NotFoundException;
import com.jwt.identity.aspects.exceptions.UnauthorizedException;
import com.jwt.identity.dto.GenericFailureResponseDto;

@ControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(NotFoundException.class)
	@ResponseStatus(value = HttpStatus.NOT_FOUND)
	public @ResponseBody GenericFailureResponseDto notFoundExceptionHandler(
			final NotFoundException notFoundExceptionHandler) {
		return new GenericFailureResponseDto(false, notFoundExceptionHandler.getMessage());
	}
  
	@ExceptionHandler(UnauthorizedException.class)
	@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
	public @ResponseBody GenericFailureResponseDto unauthorizedException(
			final UnauthorizedException unauthorizedException) {
		return new GenericFailureResponseDto(false, unauthorizedException.getMessage());
	}

	@ExceptionHandler(BadRequestException.class)
	@ResponseStatus(value = HttpStatus.BAD_REQUEST)
	public @ResponseBody GenericFailureResponseDto badRquestException(final BadRequestException badRquestException) {
		return new GenericFailureResponseDto(false, badRquestException.getMessage());
	}
	
	@ExceptionHandler(ConflictException.class)
	@ResponseStatus(value = HttpStatus.CONFLICT)
	public @ResponseBody GenericFailureResponseDto conflictException(final ConflictException conflictException) {
		return new GenericFailureResponseDto(false, conflictException.getMessage());
	}

}
