package com.smsc.management.app.gateway.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = MessagePriorityRequiredValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface MessagePriorityRequired {
    String message() default "At least one message priority must be greater than 0";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
