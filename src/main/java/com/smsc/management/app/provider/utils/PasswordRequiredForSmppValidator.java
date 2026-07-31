package com.smsc.management.app.provider.utils;

import com.smsc.management.app.provider.dto.ServiceProviderDTO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordRequiredForSmppValidator implements ConstraintValidator<PasswordRequiredForSmpp, ServiceProviderDTO> {

    @Override
    public boolean isValid(ServiceProviderDTO dto, ConstraintValidatorContext context) {
        if (dto == null) return true;

        boolean isSmpp = "SMPP".equals(dto.getProtocol());
        boolean passwordMissing = dto.getPassword() == null || dto.getPassword().isBlank();

        if (isSmpp && passwordMissing) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Password is required when protocol is SMPP")
                    .addPropertyNode("password")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}