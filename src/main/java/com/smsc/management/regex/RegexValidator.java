package com.smsc.management.regex;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * A validator for validating strings against a regular expression pattern.
 * This validator is used in conjunction with the {@link ValidRegex} annotation.
 */
public class RegexValidator implements ConstraintValidator<ValidRegex, String> {
	
	/**
     * Initializes the validator.
     *
     * @param constraintAnnotation The annotation instance to initialize with.
     */
	@Override
    public void initialize(ValidRegex constraintAnnotation) {
		// No initialization needed for this validator.
    }

	/**
     * Validates whether the given string matches the regular expression pattern.
     *
     * @param value   The string value to be validated.
     * @param context The validation context.
     * @return True if the string is valid according to the regular expression pattern, false otherwise.
     */
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }

        try {
            Pattern.compile(value);
            return true;
        } catch (PatternSyntaxException exception) {
            return false;
        }
    }
}
