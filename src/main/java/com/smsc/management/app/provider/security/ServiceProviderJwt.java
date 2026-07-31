package com.smsc.management.app.provider.security;

import com.smsc.management.app.provider.model.entity.ServiceProvider;
import com.smsc.management.utils.AppProperties;
import com.smsc.management.utils.Constants;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ServiceProviderJwt {

    private final AppProperties appProperties;
    private final Clock clock;

    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        String secret = appProperties.getServiceProviderJwtSecret();
        if (!StringUtils.hasText(secret)) {
            throw new IllegalStateException("jwt.service.provider.secret is required");
        }

        byte[] decodedSecret;
        try {
            decodedSecret = Decoders.BASE64.decode(secret);
        } catch (Exception ex) {
            throw new IllegalStateException("jwt.service.provider.secret must be a valid Base64 value", ex);
        }

        if (decodedSecret.length < Constants.MIN_SECRET_BYTES) {
            throw new IllegalStateException("jwt.service.provider.secret must decode to at least 32 bytes");
        }

        this.secretKey = Keys.hmacShaKeyFor(decodedSecret);
    }

    public GeneratedServiceProviderJwt generateBearerToken(ServiceProvider serviceProvider, long expirationSeconds) {
        validateServiceProvider(serviceProvider);

        if (expirationSeconds <= 0) {
            throw new IllegalArgumentException(Constants.ERROR_EXPIRATION_INVALID);
        }

        long iat = nowEpochSeconds();
        long nbf = iat - appProperties.getServiceProviderJwtClockSkewSeconds();
        long exp = iat + expirationSeconds;
        String jti = UUID.randomUUID().toString();

        String token = Jwts.builder()
                .setIssuer(requireIssuer())
                .setSubject(buildSubject(serviceProvider))
                .setAudience(Constants.AUDIENCE_HTTP_SERVER)
                .setIssuedAt(Date.from(Instant.ofEpochSecond(iat)))
                .setNotBefore(Date.from(Instant.ofEpochSecond(nbf)))
                .setExpiration(Date.from(Instant.ofEpochSecond(exp)))
                .setId(jti)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();

        return new GeneratedServiceProviderJwt(token, jti, exp);
    }

    public GeneratedServiceProviderJwt generateApiKeyToken(ServiceProvider serviceProvider) {
        validateServiceProvider(serviceProvider);

        long iat = nowEpochSeconds();
        long nbf = iat - appProperties.getServiceProviderJwtClockSkewSeconds();
        String jti = UUID.randomUUID().toString();

        String token = Jwts.builder()
                .setIssuer(requireIssuer())
                .setSubject(buildSubject(serviceProvider))
                .setAudience(Constants.AUDIENCE_HTTP_SERVER)
                .setIssuedAt(Date.from(Instant.ofEpochSecond(iat)))
                .setNotBefore(Date.from(Instant.ofEpochSecond(nbf)))
                .setId(jti)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();

        return new GeneratedServiceProviderJwt(token, jti, null);
    }

    private String requireIssuer() {
        String issuer = appProperties.getServiceProviderJwtIss();
        if (!StringUtils.hasText(issuer)) {
            throw new IllegalStateException("jwt.service.provider.iss is required");
        }
        return issuer;
    }

    private String buildSubject(ServiceProvider serviceProvider) {
        return serviceProvider.getSystemId()
                + Constants.SUBJECT_SEPARATOR
                + serviceProvider.getNetworkId();
    }

    private long nowEpochSeconds() {
        return clock.instant().getEpochSecond();
    }

    private void validateServiceProvider(ServiceProvider serviceProvider) {
        if (serviceProvider == null) {
            throw new IllegalArgumentException(Constants.ERROR_SP_NULL);
        }
        if (!Constants.PROTOCOL_HTTP.equalsIgnoreCase(serviceProvider.getProtocol())) {
            throw new IllegalArgumentException(Constants.ERROR_PROTOCOL_HTTP_ONLY);
        }
        if (!StringUtils.hasText(serviceProvider.getSystemId())) {
            throw new IllegalArgumentException(Constants.ERROR_SYSTEM_ID_BLANK);
        }
        if (serviceProvider.getNetworkId() <= 0) {
            throw new IllegalArgumentException(Constants.ERROR_NETWORK_ID_INVALID);
        }
    }
}