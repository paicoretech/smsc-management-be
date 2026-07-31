package com.smsc.management.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.smsc.management.utils.Constants.REGEX_FOR_VIOLATES_CONSTRAINT;

public class ResponseMapping {
	private static final String ERROR = "error";
	private static final String UNAUTHORIZED = "unauthorized";

	private ResponseMapping() {
		throw new IllegalStateException("Utility class");
	}

	public static ApiResponse errorMessage(String message) {
		return new ApiResponse(400, ERROR, message, null);
	}

	public static ApiResponse successMessage(String message, Object data) {
		return new ApiResponse(200, "success", message, data);
	}

	public static ApiResponse exceptionMessage(String message, Exception e) {
		String cause = e.getMessage();
		return new ApiResponse(500, ERROR, message + " (" + cause + ")", null);
	}
	
	public static ApiResponse errorMessageNoFound(String message) {
		return new ApiResponse(404, ERROR, message, null);
	}

	public static ApiResponse exceptionConstrainMessage(String message, Exception e) {
		String cause = e.getMessage();
		String errorDetail = extractErrorDetail(cause);
		return new ApiResponse(500, ERROR, message + " (" + errorDetail + ")", null);
	}

	private static String extractErrorDetail(String errorMessage) {
		Pattern pattern = Pattern.compile(REGEX_FOR_VIOLATES_CONSTRAINT);
		Matcher matcher = pattern.matcher(errorMessage);

		if (matcher.find()) {
			return matcher.group(0);
		}
		return errorMessage;
	}

	public static ApiResponse forbiddenErrorMessage(String message, Exception e) {
		String cause = e.getMessage();
		return new ApiResponse(403, UNAUTHORIZED, message + " (" + cause + ")", null);
	}
}
