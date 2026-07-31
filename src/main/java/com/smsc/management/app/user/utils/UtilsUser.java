package com.smsc.management.app.user.utils;

import com.paicbd.smsc.utils.Generated;
import com.smsc.management.app.user.dto.UsersDTO;
import com.smsc.management.app.user.model.entity.Users;
import com.smsc.management.app.user.model.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.ArrayList;
import java.util.List;

public class UtilsUser {
    @Generated
    private UtilsUser() {
        throw new IllegalStateException("Utility class");
    }

    public static boolean isRoot() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        return auth != null && auth.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ROOT"));
    }

    public static boolean hasPermissionForActionsAdvanced() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        return auth != null && auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_ACTIONS_ADVANCED") || role.equals("ROLE_ROOT"));
    }

    public static String encodePassword(String password) {
        return new BCryptPasswordEncoder().encode(password);
    }

    public static List<UsersDTO> converterUsersToUsersDTOs(List<Users> users) {
        return users.stream()
                .map(u -> {
                    List<String> filteredRoles = new ArrayList<>(u.getRoles());

                    if (!UtilsUser.isRoot()) {
                        filteredRoles.remove("ACTIONS_ADVANCED");
                    }

                    List<String> userSenderIds = u.getSenderIds() != null
                            ? new ArrayList<>(u.getSenderIds())
                            : new ArrayList<>();

                    return new UsersDTO(
                            u.getId(),
                            u.getName(),
                            u.getLastName(),
                            u.getUsername(),
                            userSenderIds,
                            null,
                            filteredRoles,
                            u.getStatus(),
                            u.isAccountLocked(),
                            u.getLockTime(),
                            null,
                            u.isAllServiceProviders());
                })
                .toList();
    }

    public static Integer getCurrentUserId(UserRepository userRepository) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            return null;
        }

        return userRepository.findByUserName(auth.getName())
                .map(Users::getId)
                .orElse(null);
    }
}
