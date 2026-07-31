package com.smsc.management.security;

import com.c4_soft.springaddons.security.oauth2.test.annotations.WithMockJwtAuth;
import com.smsc.management.integration.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import com.paicbd.smsc.utils.RedisManager;

import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@RunWith(SpringRunner.class)
class SecurityConfigTest extends BaseIntegrationTest {

    @Autowired
    MockMvc api;

    @MockBean
    private RedisManager redisManager;

    private static  final String JWT_AUTH_MOCK_STRING = "SCOPE_openid";
    private static final String AUTH_STRING_TEST = "fcb13146-ecd7-46a5-b9cb-a1e75fae9bdc";

    @Test
    @WithMockJwtAuth(JWT_AUTH_MOCK_STRING)
    void accessAuthUrlWithNoHeaderTest() throws Exception {
        api.perform(get("/ws/"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockJwtAuth(JWT_AUTH_MOCK_STRING)
    void authorizationParsingTest() throws Exception {
        api.perform(get("/ws")
                        .header("Authorization", AUTH_STRING_TEST)
                        .servletPath("/ws")
                )
                .andExpect(status().isOk());
    }

    @Test
    @WithMockJwtAuth(JWT_AUTH_MOCK_STRING)
    void authorizationParsingAnyTest() throws Exception {
        api.perform(get("/ws")
                        .header("Authorization", AUTH_STRING_TEST.replace("-", ""))
                        .servletPath("/ws")
                )
                .andExpect(status().isOk());
    }

    @Test
    @WithMockJwtAuth(JWT_AUTH_MOCK_STRING)
    void accessAuthNonExistingUrlTest() throws Exception {
        api.perform(get("/ws/AnotherUrl"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockJwtAuth(JWT_AUTH_MOCK_STRING)
    void accessAuthNonExistingHeaderTest() throws Exception {
        api.perform(get("/ws")
                        .header("NonExistingHeader", "")
                        .servletPath("/ws")
                )
                .andExpect(status().isOk());
    }

    @Test
    void accessDiameterResourceWithNoAuth() throws Exception {
        api.perform(get("/diameter-config/create"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockJwtAuth(JWT_AUTH_MOCK_STRING)
    void invalidUrlTest() throws Exception {
        api.perform(get("/balance-credit/any-url").servletPath("/balance-credit/any-url"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockJwtAuth(JWT_AUTH_MOCK_STRING)
    void return4xErrorResponseTest() throws Exception {
        api.perform(get("/balance-credit/credit-used")
                        .header("X-API-Key", "Cn62uZGdSUeGqmtVnHmI7iaji3C74bRd")
                        .servletPath("/balance-credit/credit-used")
                )
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockJwtAuth(JWT_AUTH_MOCK_STRING)
    void testInvalidApiKeyValueTest() throws Exception {
        api.perform(get("/balance-credit/credit-used")
                        .header("X-API-Key", AUTH_STRING_TEST.substring(0, 3))
                        .servletPath("/balance-credit/credit-used")
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void accessSs7ResourceWithNoAuth() throws Exception {
        api.perform(get("/ss7-gateways/refresh-setting"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockJwtAuth(JWT_AUTH_MOCK_STRING)
    void nonExistingHeaderAndEmptyValueTest() throws Exception {
        api.perform(get("/balance-credit/credit-used")
                        .header("NonExistingHeader", "")
                        .servletPath("/balance-credit/credit-used")
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void nonExistingHeaderAndTokenEmptyValueTest() throws Exception {
        Mockito.when(redisManager.exists(anyString())).thenReturn(false);
        api.perform(get("/broadcast/download/logs/xxx-test-123")
                        .header("NonExistingHeader", "")
                        .servletPath("/broadcast/download/logs/xxx-test-123")
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void authenticateTest() throws Exception {
        api.perform(get("/auth/authenticate")
                        .header("NonExistingHeader", "")
                        .servletPath("/auth/authenticate")
                        .content("{ \"userName\": \"admin\", \"password\": \"admin\"}")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().is4xxClientError());
    }
}
