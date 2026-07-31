package com.smsc.management.app.user.service;

import com.smsc.management.app.user.dto.AuthRequestDTO;
import com.smsc.management.app.user.dto.AuthResponseDTO;
import com.smsc.management.app.user.model.entity.Users;
import com.smsc.management.app.user.model.repository.UserRepository;
import com.smsc.management.app.user.utils.UserRole;
import com.smsc.management.security.JwtService;
import com.smsc.management.utils.ApiResponse;
import com.smsc.management.utils.ResponseMapping;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Objects;


@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository repository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public ApiResponse authenticate(AuthRequestDTO request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUserName(),
                            request.getPassword()
                    )
            );
            var user = repository.findByUserNameAndStatus(request.getUserName(), (short) 1)
                    .orElseThrow();

            var jwtToken = jwtService.generateToken(user);
            AuthResponseDTO authData = AuthResponseDTO.builder()
                    .userId(user.getId())
                    .accessToken(jwtToken)
                    .userName(user.getUsername())
                    .mustChangePassword(user.isMustChangePassword())
                    .roles(user.getRoles())
                    .senderId(user.getSenderIds() != null ? new ArrayList<>(user.getSenderIds()) : new ArrayList<>())
                    .build();

            user.setLogin(true);
            user.setJwt(jwtToken);
            repository.save(user);
            return ResponseMapping.successMessage("login successfully", authData);
        } catch (BadCredentialsException e) {
            return ResponseMapping.forbiddenErrorMessage("Username or password is incorrect", e);
        } catch (LockedException e) {
            return ResponseMapping.forbiddenErrorMessage("Account is locked", e);
        } catch (DisabledException e) {
            return ResponseMapping.forbiddenErrorMessage("Account is disabled", e);
        } catch (Exception e) {
            log.error("Error authenticate user {} -> {}", request.getUserName(), e.getMessage(), e);
            return ResponseMapping.forbiddenErrorMessage(e.getMessage(), e);
        }
    }

    public ApiResponse resetPassword(AuthRequestDTO request, boolean isByFirstLogin) {
        try {
            Users currentUser = repository.findByUserName(request.getUserName()).orElseThrow(() -> new UsernameNotFoundException("Username not found"));
            this.validateUserForFirstLogin(currentUser.getUsername(), isByFirstLogin);
            this.validateRootPasswordChange(currentUser);
            boolean isSamePassword = this.equalPassword(request.getPassword(), currentUser.getPassword());
            if (isSamePassword) {
                throw new AccessDeniedException("New password cannot be the same as the current password");
            }
            if (isByFirstLogin) {
                currentUser.setMustChangePassword(isSamePassword);
            } else {
                currentUser.setMustChangePassword(!isSamePassword);
            }

            String password = new BCryptPasswordEncoder().encode(request.getPassword());
            currentUser.setPassword(password);
            repository.save(currentUser);
            return ResponseMapping.successMessage("Reset password successfully", null);
        } catch (Exception e) {
            log.error("Error reset password {}", e.getMessage(), e);
            return ResponseMapping.exceptionMessage("Error reset password", e);
        }
    }

    public ApiResponse logOut() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (Objects.isNull(authentication)) {
                throw new AccessDeniedException("Longin not allowed to log out");
            }

            String currentUsername = authentication.getName();
            Users currenUser = repository.findByUserName(currentUsername).orElseThrow(() -> new UsernameNotFoundException("Invalid username"));
            currenUser.setLogin(false);
            repository.save(currenUser);
            return ResponseMapping.successMessage("Logout successfully", null);
        } catch (Exception e) {
            log.error("Error log out {}", e.getMessage(), e);
            return ResponseMapping.exceptionMessage("Error log out", e);
        }
    }

    private boolean equalPassword(String password1, String password2) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder.matches(password1, password2);
    }

    private void validateUserForFirstLogin(String currentUsername, boolean isByFirstLogin) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String loginUsername = authentication.getName();
        Users userLogin = repository.findByUserName(loginUsername).orElseThrow(() -> new UsernameNotFoundException("Invalid username"));

        boolean validateUser = (!userLogin.getRoles().contains(UserRole.ROOT.name()) && !userLogin.getRoles().contains(UserRole.ADMINISTRATOR.name())) || isByFirstLogin;
        if (validateUser && !currentUsername.equals(loginUsername)) {
            throw new AccessDeniedException("You are not allowed to reset password for another user.");
        }
    }

    private void validateRootPasswordChange(Users targetUser) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String loginUsername = authentication.getName();
        Users userLogin = repository.findByUserName(loginUsername)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid username"));

        boolean isTargetRoot = targetUser.getRoles().contains(UserRole.ROOT.name());

        boolean isLoginUserRoot = userLogin.getRoles().contains(UserRole.ROOT.name());

        if (isTargetRoot && !isLoginUserRoot) {
            throw new AccessDeniedException("Only ROOT users can change ROOT password");
        }
    }

}
