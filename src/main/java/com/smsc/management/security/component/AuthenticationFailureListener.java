package com.smsc.management.security.component;

import com.smsc.management.app.user.model.repository.UserRepository;
import com.smsc.management.utils.AppProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

@Component
@Slf4j
@RequiredArgsConstructor
public class AuthenticationFailureListener implements ApplicationListener<AuthenticationFailureBadCredentialsEvent> {
    private final UserRepository usersRepository;
    private final AppProperties appProperties;

    @Override
    public void onApplicationEvent(AuthenticationFailureBadCredentialsEvent event) {
        String username = (String) event.getAuthentication().getPrincipal();

        usersRepository.findByUserNameAndStatus(username, (short) 1).ifPresent(user -> {
            int totalFailedLoginAttempts = user.getFailedLoginAttempts();
            LocalDateTime currentDateTime = LocalDateTime.now();
            LocalDateTime lastFailedLoginTime = Objects.nonNull(user.getLastFailedLoginTime()) ? user.getLastFailedLoginTime() : currentDateTime;
            Duration duration = Duration.between(lastFailedLoginTime, currentDateTime);
            double hoursFromLastFailedLogin = duration.getSeconds()/3600.0;

            if (hoursFromLastFailedLogin > appProperties.getFailedLoginAttemptHours()) {
                totalFailedLoginAttempts = 0;
            }

            int attempts = totalFailedLoginAttempts + 1;
            user.setFailedLoginAttempts(attempts);
            user.setLastFailedLoginTime(currentDateTime);

            if (attempts >= appProperties.getFailedLoginAttempts()) {
                user.setAccountLocked(true);
                user.setLockTime(LocalDateTime.now());
            }

            usersRepository.save(user);
        });
    }
}
