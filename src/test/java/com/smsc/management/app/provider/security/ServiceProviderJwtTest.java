package com.smsc.management.app.provider.security;
import com.smsc.management.app.provider.model.entity.ServiceProvider;
import com.smsc.management.utils.AppProperties;
import com.smsc.management.utils.Constants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import javax.crypto.SecretKey;
import java.lang.reflect.Field;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ServiceProviderJwtTest {

    private static final Instant FIXED_NOW = Instant.parse("2026-04-22T18:00:00Z");

    private ServiceProviderJwt serviceProviderJwt;
    private SecretKey verificationKey;

    @BeforeEach
    void setUp() throws Exception {
        String base64Secret = "MzAxMjM0NTY3ODkwMTIzNDU2Nzg5MDEyMzQ1Njc4OTA=";

        AppProperties appProperties = new AppProperties();
        setField(appProperties, "serviceProviderJwtSecret", base64Secret);
        setField(appProperties, "serviceProviderJwtIss", "smsc-management");
        setField(appProperties, "serviceProviderJwtClockSkewSeconds", 30L);

        verificationKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(base64Secret));

        serviceProviderJwt = new ServiceProviderJwt(
                appProperties,
                Clock.fixed(FIXED_NOW, ZoneOffset.UTC)
        );
        serviceProviderJwt.init();
    }

    @Test
    void shouldGenerateBearerTokenWithExpectedClaims() {
        ServiceProvider serviceProvider = buildHttpServiceProvider();

        GeneratedServiceProviderJwt generated = serviceProviderJwt.generateBearerToken(serviceProvider, 300);

        Claims claims = parseClaims(generated.token());

        assertEquals("smsc-management", claims.getIssuer());
        assertEquals("system-1|101", claims.getSubject());
        assertEquals(Constants.AUDIENCE_HTTP_SERVER, claims.getAudience());
        assertEquals(Date.from(FIXED_NOW), claims.getIssuedAt());
        assertEquals(Date.from(FIXED_NOW.minusSeconds(30)), claims.getNotBefore());
        assertEquals(Date.from(FIXED_NOW.plusSeconds(300)), claims.getExpiration());
        assertEquals(generated.jti(), claims.getId());
        assertDoesNotThrow(() -> UUID.fromString(claims.getId()));
    }

    @Test
    void shouldGenerateApiKeyTokenWithoutExpirationClaim() {
        ServiceProvider serviceProvider = buildHttpServiceProvider();

        GeneratedServiceProviderJwt generated = serviceProviderJwt.generateApiKeyToken(serviceProvider);

        Claims claims = parseClaims(generated.token());

        assertEquals("smsc-management", claims.getIssuer());
        assertEquals("system-1|101", claims.getSubject());
        assertEquals(Constants.AUDIENCE_HTTP_SERVER, claims.getAudience());
        assertEquals(Date.from(FIXED_NOW), claims.getIssuedAt());
        assertEquals(Date.from(FIXED_NOW.minusSeconds(30)), claims.getNotBefore());
        assertNull(claims.getExpiration());
        assertEquals(generated.jti(), claims.getId());
        assertDoesNotThrow(() -> UUID.fromString(claims.getId()));
    }

    @Test
    void shouldFailWhenExpirationSecondsIsInvalid() {
        ServiceProvider serviceProvider = buildHttpServiceProvider();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> serviceProviderJwt.generateBearerToken(serviceProvider, 0)
        );

        assertEquals(Constants.ERROR_EXPIRATION_INVALID, exception.getMessage());
    }

    @Test
    void shouldFailWhenServiceProviderIsNull() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> serviceProviderJwt.generateApiKeyToken(null)
        );

        assertEquals(Constants.ERROR_SP_NULL, exception.getMessage());
    }

    @Test
    void shouldFailWhenProtocolIsNotHttp() {
        ServiceProvider serviceProvider = buildHttpServiceProvider();
        serviceProvider.setProtocol("SMPP");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> serviceProviderJwt.generateApiKeyToken(serviceProvider)
        );

        assertEquals(Constants.ERROR_PROTOCOL_HTTP_ONLY, exception.getMessage());
    }

    @Test
    void shouldFailWhenSystemIdIsBlank() {
        ServiceProvider serviceProvider = buildHttpServiceProvider();
        serviceProvider.setSystemId(" ");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> serviceProviderJwt.generateApiKeyToken(serviceProvider)
        );

        assertEquals(Constants.ERROR_SYSTEM_ID_BLANK, exception.getMessage());
    }

    @Test
    void shouldFailWhenNetworkIdIsInvalid() {
        ServiceProvider serviceProvider = buildHttpServiceProvider();
        serviceProvider.setNetworkId(0);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> serviceProviderJwt.generateApiKeyToken(serviceProvider)
        );

        assertEquals(Constants.ERROR_NETWORK_ID_INVALID, exception.getMessage());
    }

    @Test
    void shouldGenerateDifferentJtiForDifferentTokens() {
        ServiceProvider serviceProvider = buildHttpServiceProvider();

        GeneratedServiceProviderJwt first = serviceProviderJwt.generateApiKeyToken(serviceProvider);
        GeneratedServiceProviderJwt second = serviceProviderJwt.generateApiKeyToken(serviceProvider);

        assertNotEquals(first.jti(), second.jti());
    }

    private ServiceProvider buildHttpServiceProvider() {
        ServiceProvider serviceProvider = new ServiceProvider();
        serviceProvider.setNetworkId(101);
        serviceProvider.setSystemId("system-1");
        serviceProvider.setProtocol(Constants.PROTOCOL_HTTP);
        return serviceProvider;
    }

    private Claims parseClaims(String token) {
        return  Jwts.parserBuilder()
                .setSigningKey(verificationKey)
                .setClock(() -> Date.from(FIXED_NOW))
                .setAllowedClockSkewSeconds(30)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = AppProperties.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}