package com.smsc.management.app.provider.utils;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = PasswordRequiredForSmppValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface PasswordRequiredForSmpp {
    String message() default "Password is required when protocol is SMPP";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
