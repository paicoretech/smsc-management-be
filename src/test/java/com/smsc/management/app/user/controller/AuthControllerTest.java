package com.smsc.management.app.user.controller;

import com.smsc.management.app.user.dto.AuthRequestDTO;
import com.smsc.management.app.user.dto.AuthResponseDTO;
import com.smsc.management.app.user.model.entity.Users;
import com.smsc.management.app.user.model.repository.UserRepository;
import com.smsc.management.app.user.utils.UserRole;
import com.smsc.management.integration.BaseIntegrationTest;
import com.smsc.management.utils.ApiResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.ArrayList;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthControllerTest extends BaseIntegrationTest {

    @Autowired
    private AuthController authController;

    @Autowired
    private UserRepository repository;

    @Test
    @WithMockUser(username = "admin")
    @DisplayName("authenticate test and reset password with the same")
    void authenticateAndResetPasswordTest() {
        AuthRequestDTO authRequestDTO = new AuthRequestDTO();
        authRequestDTO.setUserName("admin");
        authRequestDTO.setPassword("admin");
        ResponseEntity<ApiResponse> response = authController.authenticate(authRequestDTO);
        assertTrue(response.getStatusCode().is2xxSuccessful());
        ApiResponse apiResponse = response.getBody();
        assertNotNull(apiResponse);
        AuthResponseDTO authResponseDTO = (AuthResponseDTO) apiResponse.data();

        authRequestDTO.setPassword("12345");
        response = authController.resetPasswd(authResponseDTO.isMustChangePassword(), authRequestDTO);
        assertTrue(response.getStatusCode().is2xxSuccessful());
    }

    @Test
    @DisplayName("authenticate test when user is locked and inactive status")
    void authenticateWhenUserIsLockedThenGetHttpErrorResponse() {
        Users users = createAndGetMockUser("admin-locked");
        users = repository.save(users);

        AuthRequestDTO authRequestDTO = new AuthRequestDTO();
        authRequestDTO.setUserName("admin-locked");
        authRequestDTO.setPassword("invalid-passwd");

        for (int i = 0; i < 4; i++) {
            ResponseEntity<ApiResponse> response = authController.authenticate(authRequestDTO);
            assertTrue(response.getStatusCode().is4xxClientError());
            ApiResponse apiResponse = response.getBody();
            assertNotNull(apiResponse);
            assertEquals("unauthorized", apiResponse.message());
            if (i < 3) {
                Users updatedUsers = repository.findById(users.getId()).orElse(null);
                assertNotNull(updatedUsers);
                assertNotNull(updatedUsers.getLastFailedLoginTime());
                boolean containsString = apiResponse.comment().toLowerCase().contains("bad credentials");
                assertTrue(containsString);
            } else {
                boolean containsString = apiResponse.comment().toLowerCase().contains("user account is locked");
                assertTrue(containsString);
            }
        }

        // login success
        users.setLockTime(null);
        users.setAccountLocked(false);
        users.setFailedLoginAttempts(0);
        repository.save(users);
        authRequestDTO.setPassword("12345");
        ResponseEntity<ApiResponse> response = authController.authenticate(authRequestDTO);
        assertTrue(response.getStatusCode().is2xxSuccessful());
        ApiResponse apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertEquals("success", apiResponse.message());

        // user status disabled
        users.setStatus((short) 0);
        repository.save(users);
        response = authController.authenticate(authRequestDTO);
        assertTrue(response.getStatusCode().is4xxClientError());
        apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertEquals("unauthorized", apiResponse.message());
        boolean containsString = apiResponse.comment().toLowerCase().contains("user is disabled");
        assertTrue(containsString);
    }

    @WithMockUser(username = "admin")
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @DisplayName("Reset password with different user and user not found")
    void resetPasswordWithDifferentUserThenHttpStatusResponseError(boolean existsUser) {
        if (existsUser) {
            Users users = createAndGetMockUser("juanPerez");
            repository.save(users);
        }

        AuthRequestDTO authRequestDTO = new AuthRequestDTO();
        authRequestDTO.setUserName("juanPerez");
        authRequestDTO.setPassword("12345");

        ResponseEntity<ApiResponse> response = authController.resetPasswd(true, authRequestDTO);
        assertTrue(response.getStatusCode().is5xxServerError());
    }

    @WithMockUser(username = "juanPerez")
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @DisplayName("Logout when user exists and user not found")
    void logOutTest(boolean existsUser) {
        if (existsUser) {
            Users users = createAndGetMockUser("juanPerez");
            repository.save(users);
        }

        ResponseEntity<ApiResponse> response = authController.logout();

        if (existsUser) {
            assertTrue(response.getStatusCode().is2xxSuccessful());
            Users userUpdated = repository.findByUserName("juanPerez").orElse(null);
            assertNotNull(userUpdated);
            assertFalse(userUpdated.isLogin());
        } else {
            assertTrue(response.getStatusCode().is5xxServerError());
        }
    }

    private Users createAndGetMockUser(String username) {
        Users user = new Users();
        user.setUserName(username);
        user.setPassword(new BCryptPasswordEncoder().encode("12345"));
        user.setRoles(new ArrayList<>(Collections.singletonList(UserRole.ROOT.getDisplayName())));
        user.setStatus((short) 1);
        user.setName("root");
        user.setLastName("PAiC");
        user.setLogin(true);
        user.setFailedLoginAttempts(0);
        user.setAccountLocked(false);
        return user;
    }
}
