package com.smsc.management.config;

import com.smsc.management.utils.AppProperties;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(MockitoExtension.class)
class CorsFilterConfigTest {
    @Mock
    AppProperties properties;
    @Mock
    FilterChain chain;
    CorsFilterConfig config;

    @BeforeEach
    void setUp() {
        this.config = new CorsFilterConfig(properties);
    }

    @Test
    void doFilter() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        assertDoesNotThrow(() -> this.config.doFilter(request, response, chain));
    }

    @Test
    void methodEqualsOptions() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("OPTIONS");
        MockHttpServletResponse response = new MockHttpServletResponse();
        assertDoesNotThrow(() -> this.config.doFilter(request, response, chain));
    }
}