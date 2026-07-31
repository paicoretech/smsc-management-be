package com.smsc.management.app.user.service;

import com.smsc.management.app.user.dto.AuthRequestDTO;
import com.smsc.management.app.user.model.entity.Users;
import com.smsc.management.app.user.model.repository.UserRepository;
import com.smsc.management.app.user.utils.UserRole;
import com.smsc.management.utils.ApiResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthService authService;

    @Test
    void testValidateRootPasswordChange_RootChangingRootPassword_Success() {
        Users rootUser = new Users();
        rootUser.setUserName("root");
        rootUser.setRoles(Arrays.asList(UserRole.ROOT.name()));
        
        when(authentication.getName()).thenReturn("root");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(userRepository.findByUserName("root")).thenReturn(Optional.of(rootUser));

        assertDoesNotThrow(() -> authService.resetPassword(createRequest("root", "newPass"), false));
    }

    @Test
    void testValidateRootPasswordChange_AdminChangingRootPassword_ThrowsAccessDeniedException() {
        Users rootUser = new Users();
        rootUser.setUserName("root");
        rootUser.setRoles(Arrays.asList(UserRole.ROOT.name()));
        
        Users adminUser = new Users();
        adminUser.setUserName("admin");
        adminUser.setRoles(Arrays.asList(UserRole.ADMINISTRATOR.name()));
        
        when(authentication.getName()).thenReturn("admin");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(userRepository.findByUserName("admin")).thenReturn(Optional.of(adminUser));
        when(userRepository.findByUserName("root")).thenReturn(Optional.of(rootUser));

        ApiResponse response = authService.resetPassword(createRequest("root", "newPass"), false);

        assertNotNull(response);
        assertEquals("error", response.message());
        assertTrue(response.comment().contains("Only ROOT users can change ROOT password"));
    }

    @Test
    void testValidateRootPasswordChange_RegularUserChangingRootPassword_ThrowsAccessDeniedException() {
        Users rootUser = new Users();
        rootUser.setUserName("root");
        rootUser.setRoles(Arrays.asList(UserRole.ROOT.name()));

        Users regularUser = new Users();
        regularUser.setUserName("user");
        regularUser.setRoles(Arrays.asList(UserRole.CAMPAIGN_OPERATOR.name()));

        when(authentication.getName()).thenReturn("user");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(userRepository.findByUserName("user")).thenReturn(Optional.of(regularUser));
        when(userRepository.findByUserName("root")).thenReturn(Optional.of(rootUser));

        ApiResponse response = authService.resetPassword(createRequest("root", "newPass"), false);

        assertNotNull(response);
        assertEquals("error", response.message());
        assertTrue(response.comment().contains("You are not allowed to reset password for another user"));
    }

    private AuthRequestDTO createRequest(String username, String password) {
        AuthRequestDTO request = new AuthRequestDTO();
        request.setUserName(username);
        request.setPassword(password);
        return request;
    }
}
