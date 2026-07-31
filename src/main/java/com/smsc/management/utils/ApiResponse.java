package com.smsc.management.utils;

public record ApiResponse(
	int status,
	String message,
	String comment,
	Object data
) {
}
