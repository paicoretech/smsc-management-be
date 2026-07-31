package com.smsc.management.app.ss7.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = TcapTimeoutValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidTimeouts {
    String message() default "invokeTimeout must not be greater than dialogIdleTimeout";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}