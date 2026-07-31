package com.smsc.management.app.ss7.validator;

import com.paicbd.smsc.utils.Generated;
import com.smsc.management.app.ss7.dto.TcapDTO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

@Generated
public class TcapTimeoutValidator implements ConstraintValidator<ValidTimeouts, TcapDTO> {

    @Override
    public boolean isValid(TcapDTO dto, ConstraintValidatorContext context) {
        if (dto == null) return true;

        if (dto.getInvokeTimeout() > dto.getDialogIdleTimeout()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                    .addPropertyNode("invokeTimeout")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}
