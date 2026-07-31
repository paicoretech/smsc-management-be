package com.smsc.management.app.user.service;

import com.smsc.management.app.user.model.entity.Users;
import com.smsc.management.app.user.model.repository.UserRepository;
import com.smsc.management.utils.AppProperties;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    AppProperties appProperties;

    @Mock
    UserRepository userRepository;

    @InjectMocks
    UserService userService;

    @Test
    void createUserWithExistingRole() {
        when(userRepository.existsByRolesContaining(anyString())).thenReturn(true);
        assertDoesNotThrow(() -> userService.createRootUser());
    }

    @Test
    void createExistingRootUser() {
        when(appProperties.getRootUser()).thenReturn("ROOT");
        when(userRepository.findByUserName(anyString())).thenReturn(Optional.of(new Users()));
        assertDoesNotThrow(() -> userService.createRootUser());
    }
}
