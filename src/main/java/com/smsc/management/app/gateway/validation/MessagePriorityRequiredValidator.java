package com.smsc.management.app.gateway.validation;

import com.smsc.management.app.diameter.dto.DiameterGatewayDTO;
import com.smsc.management.app.gateway.dto.GatewaysDTO;
import com.smsc.management.app.ss7.dto.Ss7GatewaysDTO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class MessagePriorityRequiredValidator implements ConstraintValidator<MessagePriorityRequired, Object> {

    @Override
    public boolean isValid(Object target, ConstraintValidatorContext context) {
        if (target == null) {
            return true;
        }

        GatewayPriorityValues gatewayPriorityValues = switch (target) {
            case GatewaysDTO gateway -> new GatewayPriorityValues(
                    gateway.getProtocol(),
                    null,
                    gateway.getMessagesPerSecondHigh(),
                    gateway.getMessagesPerSecondMedium(),
                    gateway.getMessagesPerSecondLow()
            );
            case Ss7GatewaysDTO gateway -> new GatewayPriorityValues(
                    gateway.getProtocol(),
                    null,
                    gateway.getMessagesPerSecondHigh(),
                    gateway.getMessagesPerSecondMedium(),
                    gateway.getMessagesPerSecondLow()
            );
            case DiameterGatewayDTO gateway -> new GatewayPriorityValues(
                    gateway.getProtocol(),
                    gateway.getType(),
                    gateway.getMessagesPerSecondHigh(),
                    gateway.getMessagesPerSecondMedium(),
                    gateway.getMessagesPerSecondLow()
            );
            default -> null;
        };

        if (gatewayPriorityValues == null || !shouldValidateGatewayPriority(gatewayPriorityValues)) {
            return true;
        }

        if (hasAnyPriorityEnabled(gatewayPriorityValues)) {
            return true;
        }

        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                .addPropertyNode("messagesPerSecondHigh")
                .addConstraintViolation();
        return false;
    }

    private boolean shouldValidateGatewayPriority(GatewayPriorityValues gatewayPriorityValues) {
        return switch (formatProtocol(gatewayPriorityValues.protocol())) {
            case "HTTP", "SMPP", "SS7" -> true;
            case "DIAMETER" -> "GATEWAY".equals(formatType(gatewayPriorityValues.type()));
            default -> false;
        };
    }

    private boolean hasAnyPriorityEnabled(GatewayPriorityValues gatewayPriorityValues) {
        return gatewayPriorityValues.high() > 0
                || gatewayPriorityValues.medium() > 0
                || gatewayPriorityValues.low() > 0;
    }

    private String formatProtocol(String protocol) {
        return protocol == null ? "" : protocol.trim().toUpperCase();
    }

    private String formatType(String type) {
        return type == null ? "" : type.trim().toUpperCase();
    }

    private record GatewayPriorityValues(String protocol, String type, int high, int medium, int low) {
    }
}
