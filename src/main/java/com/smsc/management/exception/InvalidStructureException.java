package com.smsc.management.exception;

import com.smsc.management.utils.ApiResponse;

import java.io.Serial;

public class InvalidStructureException extends Exception {
	@Serial
	private static final long serialVersionUID = 1L;
	
	public InvalidStructureException(String message) {
		super(message);
	}
	
	public ApiResponse exceptionMessage(String message, InvalidStructureException e) {
		String cause = e.getMessage();
		return new ApiResponse(400, "error", message + "(" + cause + ")", null);
	}
}
