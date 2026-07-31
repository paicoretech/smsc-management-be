package com.smsc.management.security;

import com.smsc.management.app.user.model.entity.Users;
import com.smsc.management.app.user.model.repository.UserRepository;
import com.smsc.management.app.user.utils.UserRole;
import com.smsc.management.integration.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ApplicationConfigTest extends BaseIntegrationTest {
    @Autowired
    private ApplicationConfig applicationConfig;

    @Autowired
    private UserRepository repository;

    @Test
    void userDetailsServiceTest() {
        Users users = createAndGetMockUser("admin_test");
        repository.save(users);
        UserDetailsService userDetailsService = applicationConfig.userDetailsService();
        UserDetails userDetails = userDetailsService.loadUserByUsername(users.getUsername());
        assertNotNull(userDetails);
    }

    @Test
    void userDetailsServiceNotFound() {
        Users users = createAndGetMockUser("guest");
        UserDetailsService userDetailsService = applicationConfig.userDetailsService();
        String userName = users.getUsername();
        assertThrows(UsernameNotFoundException.class, () -> userDetailsService.loadUserByUsername(userName));
    }

    private Users createAndGetMockUser(String userName) {
        Users user = new Users();
        user.setUserName(userName);
        user.setPassword("admin");
        user.setRoles(new ArrayList<>(Collections.singletonList(UserRole.ROOT.getDisplayName())));
        user.setStatus((short) 1);
        user.setName(userName.toUpperCase());
        user.setLastName("PAiC");
        return user;
    }
}
