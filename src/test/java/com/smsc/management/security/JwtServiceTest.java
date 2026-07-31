package com.smsc.management.security;

import com.smsc.management.utils.AppProperties;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import static org.mockito.Mockito.when;

@Slf4j
@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @Mock
    AppProperties appProperties;

    @Mock
    UserDetails userDetails;

    @InjectMocks
    JwtService jwtService;

    private final String SECRET_KEY_TEST = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private final String USER_NAME_TEST = "admin";
    private final long TOKEN_VALIDITY_TIME_TEST = 8_000L;

    @Test
    void extractUserNameTest() {
        when(appProperties.getSecretKey()).thenReturn(SECRET_KEY_TEST);
        when(appProperties.getJwtExpiration()).thenReturn(TOKEN_VALIDITY_TIME_TEST);
        when(userDetails.getUsername()).thenReturn(USER_NAME_TEST);
        String jwt = jwtService.generateToken(userDetails);
        assertNotNull(jwtService.extractUsername(jwt));
    }

    @Test
    void generateTokenTest() {
        when(appProperties.getSecretKey()).thenReturn(SECRET_KEY_TEST);
        assertNotNull(jwtService.generateToken(userDetails));
    }

    @Test
    void isTokenValidWithExistingUserTest() {
        when(appProperties.getSecretKey()).thenReturn(SECRET_KEY_TEST);
        when(appProperties.getJwtExpiration()).thenReturn(TOKEN_VALIDITY_TIME_TEST);
        when(userDetails.getUsername()).thenReturn(USER_NAME_TEST);
        String jwt = jwtService.generateToken(userDetails);
        assertTrue(jwtService.isTokenValid(jwt, userDetails));
    }

    @Test
    void isTokenValidWithNonExistingUserTest() {
        when(appProperties.getSecretKey()).thenReturn(SECRET_KEY_TEST);
        when(appProperties.getJwtExpiration()).thenReturn(TOKEN_VALIDITY_TIME_TEST);
        when(userDetails.getUsername()).thenReturn(USER_NAME_TEST).thenReturn(USER_NAME_TEST + "2");
        String jwt = jwtService.generateToken(userDetails);
        assertFalse(jwtService.isTokenValid(jwt, userDetails));
    }
}
