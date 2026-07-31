package com.smsc.management.app.provider.dto;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GenerateServiceProviderSecurityTokenRequestDTO {

    @Pattern(regexp = "^(Bearer|Api-key)$", message = "security_authentication_type must be Bearer or Api-key")
    @JsonProperty("security_authentication_type")
    private String securityAuthenticationType;

    @JsonProperty("bearer_token_expiration_seconds")
    @Min(value = 1, message = "bearer_token_expiration_seconds must be greater than 0")
    private Long bearerTokenExpirationSeconds;
}