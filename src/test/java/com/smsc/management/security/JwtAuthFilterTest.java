package com.smsc.management.security;

import com.smsc.management.app.user.model.repository.UserRepository;
import com.smsc.management.utils.AppProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.io.IOException;
import java.util.Collections;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Slf4j
@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    @Mock
    HttpServletRequest request;

    @Mock
    HttpServletResponse response;

    @Mock
    FilterChain filterChain;

    @Mock
    UserDetails userDetails;

    @Mock
    AppProperties appProperties;

    @Mock
    UserDetailsService userDetailsService;

    @Mock
    JwtService jwtService;

    @Mock
    UserRepository userRepository;

    @InjectMocks
    JwtAuthFilter jwtFilter;

    private final String TEST_JWT = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ0b2tlbl90eXBlIjoiYWNjZXNzIiwiZXhwIjoxNjk5NDIwNTAwLCJpYXQiOjE2OTk0MTY5MDAsImp0aSI6Ijg3N2Q5YTEzNDUxOTQ2NmRhMGM4YmQwMzZmNDQ2ZDU3IiwidXNlcl9pZCI6MX0.BPS0eyPdHPkEX2YBRptaWPT8gIHICNPkz72Fc2FW1mI";
    private final String TEST_USER_NAME = "admin";

    @Test
    void doInternalFilterWithValidTokenTest() throws ServletException, IOException {
        when(request.getServletPath()).thenReturn("/authenticate");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + TEST_JWT);
        when(jwtService.extractUsername(TEST_JWT)).thenReturn(TEST_USER_NAME);
        when(userRepository.existsByUserNameAndLoginAndJwt(eq(TEST_USER_NAME), eq(true), anyString())).thenReturn(true);
        when(userDetailsService.loadUserByUsername(TEST_USER_NAME)).thenReturn(userDetails);
        when(jwtService.isTokenValid(TEST_JWT, userDetails)).thenReturn(true);
        when(userDetails.getAuthorities()).thenReturn(Collections.emptyList());
        jwtFilter.doFilterInternal(request, response, filterChain);
        verify(filterChain, times(1)).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext());
    }

    @Test
    void doInternalFilterChainTest() throws ServletException, IOException {
        when(request.getServletPath()).thenReturn("/authenticate");
        jwtFilter.doFilterInternal(request, response, filterChain);
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void doInternalFilterWithUserNameTest() throws ServletException, IOException {
        doInternalFilterAndUsersTest(TEST_USER_NAME);
    }

    @Test
    void doInternalFilterNoUserNameTest() throws ServletException, IOException {
        doInternalFilterAndUsersTest(null);
    }

    private void doInternalFilterAndUsersTest(String userName) throws ServletException, IOException {
        when(request.getServletPath()).thenReturn("/auth");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + TEST_JWT);
        when(jwtService.extractUsername(TEST_JWT)).thenReturn(userName);
        if (Objects.nonNull(userName)) {
            when(userRepository.existsByUserNameAndLoginAndJwt(eq(userName), eq(true), anyString())).thenReturn(true);
        }
        jwtFilter.doFilterInternal(request, response, filterChain);
        verify(filterChain, times(1)).doFilter(request, response);
    }
}
