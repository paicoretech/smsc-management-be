package com.smsc.management.security.component;

import com.smsc.management.app.user.model.entity.Users;
import com.smsc.management.app.user.model.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class AuthenticationSuccessListener implements ApplicationListener<AuthenticationSuccessEvent> {
    private final UserRepository usersRepository;

    @Override
    public void onApplicationEvent(AuthenticationSuccessEvent event) {
        var userSuccess = (Users) event.getAuthentication().getPrincipal();

        usersRepository.findByUserName(userSuccess.getUsername()).ifPresent(user -> {
            user.setFailedLoginAttempts(0);
            user.setAccountLocked(false);
            user.setLockTime(null);
            user.setLastFailedLoginTime(null);
            usersRepository.save(user);
        });
    }
}
