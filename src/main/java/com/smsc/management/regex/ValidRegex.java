package com.smsc.management.regex;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Documented;


/**
 * An annotation used to validate whether a string corresponds to a valid regex.
 * This annotation is used in conjunction with the {@link RegexValidator} class.
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = RegexValidator.class)
@Documented
public @interface ValidRegex {
	
	/**
     * Specifies the message to be shown when the validation fails.
     *
     * @return The validation failure message.
     */
    String message() default "The value is not a valid regex";
    
    /**
     * Specifies the validation groups to which this constraint belongs.
     *
     * @return An array of validation groups.
     */
    Class<?>[] groups() default {};
    
    /**
     * Specifies the payload associated with this constraint.
     *
     * @return An array of payload classes.
     */
    Class<? extends Payload>[] payload() default {};
}
