package com.smsc.management.app.user.service;

import com.smsc.management.app.user.dto.UserRoleDto;
import com.smsc.management.app.user.dto.UsersDTO;
import com.smsc.management.app.user.mapper.UserMapper;
import com.smsc.management.app.user.model.entity.UserServiceProvider;
import com.smsc.management.app.user.model.repository.UserServiceProviderRepository;
import com.smsc.management.app.provider.model.repository.ServiceProviderRepository;
import com.smsc.management.app.provider.model.entity.ServiceProvider;
import com.smsc.management.app.user.model.entity.Users;
import com.smsc.management.app.user.model.repository.UserRepository;
import com.smsc.management.app.user.utils.UserRole;
import com.smsc.management.app.user.utils.UtilsUser;
import com.smsc.management.exception.SmscBackendException;
import com.smsc.management.utils.ApiResponse;
import com.smsc.management.utils.AppProperties;
import com.smsc.management.utils.ResponseMapping;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static com.smsc.management.utils.Constants.DISABLED;

/**
 * Service class for managing users.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final AppProperties appProperties;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UserServiceProviderRepository userServiceProviderRepository;
    private final ServiceProviderRepository serviceProviderRepository;

    public List<UserRoleDto> getAllRoles() {
        boolean isRoot = UtilsUser.isRoot();

        return Stream.of(UserRole.values())
                .filter(role -> (isRoot && role != UserRole.ROOT) || (role != UserRole.ROOT && role != UserRole.ACTIONS_ADVANCED))
                .map(role -> new UserRoleDto(role.name(), role.getDisplayName()))
                .toList();
    }

    public ApiResponse getAllUsers() {
        try {
            List<Users> users = userRepository.findUsersAll();
            List<UsersDTO> usersDTOS = UtilsUser.converterUsersToUsersDTOs(users);

            List<Integer> allHttpProviderIds = serviceProviderRepository.findByEnabledNot(2).stream()
                    .filter(sp -> "HTTP".equalsIgnoreCase(sp.getProtocol()))
                    .map(ServiceProvider::getNetworkId)
                    .toList();

            for (UsersDTO userDTO : usersDTOS) {
                Users user = users.stream().filter(u -> u.getId().equals(userDTO.getId())).findFirst().orElse(null);
                if (Objects.nonNull(user) && user.isAllServiceProviders()) {
                    userDTO.setServiceProviders(allHttpProviderIds);
                    userDTO.setAllServiceProviders(true);
                } else {
                    List<UserServiceProvider> assignments = userServiceProviderRepository.findByUserId(userDTO.getId());
                    List<Integer> providerIds = assignments.stream()
                            .map(usp -> usp.getServiceProvider().getNetworkId())
                            .toList();
                    userDTO.setServiceProviders(providerIds);
                    userDTO.setAllServiceProviders(false);
                }
            }

            return ResponseMapping.successMessage("Get users request success", usersDTOS);
        } catch (Exception e) {
            log.error("Get users list with error: {}", e.getMessage());
            return ResponseMapping.exceptionMessage("Users was end with error while getting data", e);
        }
    }

    @Transactional
    public ApiResponse createUser(UsersDTO usersDTO) {
        try {
            String password = usersDTO.getPassword();
            if (Objects.isNull(password) || password.isBlank()) {
                throw new SmscBackendException("Password cannot be empty");
            }
            usersDTO.setPassword(UtilsUser.encodePassword(password));
            boolean isAdmin = usersDTO.getRoles().contains(UserRole.ROOT.name()) ||
                    usersDTO.getRoles().contains(UserRole.ADMINISTRATOR.name());
            if (isAdmin) {
                usersDTO.setAllServiceProviders(true);
            }

            Users newUser = userMapper.toEntity(usersDTO);
            List<String> cleanedSenderIds = sanitizeSenderIds(usersDTO.getSenderIds());
            newUser.setSenderIds(cleanedSenderIds);
            Users userResult = userRepository.save(newUser);

            if (userResult.isAllServiceProviders()) {
                userServiceProviderRepository.deleteByUserId(userResult.getId());
                userServiceProviderRepository.flush();

            } else if (usersDTO.getServiceProviders() != null && !usersDTO.getServiceProviders().isEmpty()) {
                assignServiceProviders(userResult, usersDTO.getServiceProviders());
            }

            UsersDTO responseDTO = userMapper.toDTO(userResult);
            responseDTO.setServiceProviders(usersDTO.getServiceProviders());
            responseDTO.setSenderIds(new ArrayList<>(userResult.getSenderIds()));
            responseDTO.setAllServiceProviders(usersDTO.isAllServiceProviders());
            responseDTO.setPassword(null);

            return ResponseMapping.successMessage("Create user success", responseDTO);
        } catch (Exception e) {
            log.error("Create user with error: {}", e.getMessage());
            return ResponseMapping.exceptionMessage("Create user with error while creating user", e);
        }
    }

    @Transactional
    public ApiResponse updateUser(int id, UsersDTO usersDTO) {
        usersDTO.setId(id);
        try {
            Users currentUser = userRepository.findById(id).orElse(null);
            if (Objects.isNull(currentUser)) {
                return ResponseMapping.errorMessageNoFound("User with id " + id + " not found");
            }
            List<String> currentRoles = currentUser.getRoles();
            boolean addActionsAdvanced = Objects.nonNull(currentRoles) && currentRoles.contains(UserRole.ACTIONS_ADVANCED.name())
                    && !usersDTO.getRoles().contains(UserRole.ACTIONS_ADVANCED.name());
            if (addActionsAdvanced && !UtilsUser.isRoot()) {
                usersDTO.getRoles().add(UserRole.ACTIONS_ADVANCED.name());
            }
            usersDTO.setPassword(null);
            userMapper.updateEntityFromDTO(usersDTO, currentUser);
            List<String> cleanedSenderIds = sanitizeSenderIds(usersDTO.getSenderIds());
            currentUser.getSenderIds().clear();
            currentUser.getSenderIds().addAll(cleanedSenderIds);
            if (!currentUser.isAccountLocked()) {
                currentUser.setFailedLoginAttempts(0);
                currentUser.setLockTime(null);
                currentUser.setLastFailedLoginTime(null);
            }

            boolean isAdmin = usersDTO.getRoles().contains(UserRole.ROOT.name()) ||
                    usersDTO.getRoles().contains(UserRole.ADMINISTRATOR.name());

            if (isAdmin) {
                currentUser.setAllServiceProviders(true);
            } else {
                currentUser.setAllServiceProviders(usersDTO.isAllServiceProviders());
            }

            if (currentUser.getStatus() == DISABLED) {
                currentUser.setLogin(false);
            }
            userRepository.save(currentUser);

            if (currentUser.isAllServiceProviders()) {
                userServiceProviderRepository.deleteByUserId(id);
                userServiceProviderRepository.flush();

            } else if (usersDTO.getServiceProviders() != null) {
                userServiceProviderRepository.deleteByUserId(id);
                userServiceProviderRepository.flush();
                assignServiceProviders(currentUser, usersDTO.getServiceProviders());
            }

            return ResponseMapping.successMessage("Update user success", null);
        } catch (Exception e) {
            log.error("Update user with error: {}", e.getMessage());
            return ResponseMapping.exceptionMessage("Update user with error while creating user", e);
        }
    }

    private void assignServiceProviders(Users user, List<Integer> providerIds) {
        for (Integer providerId : providerIds) {
            ServiceProvider sp = serviceProviderRepository.findByNetworkIdAndProtocolAndEnabledNot(
                    providerId, "HTTP", -1);
            if (sp == null) {
                sp = serviceProviderRepository.findByNetworkId(providerId);
            }

            if (sp != null && "HTTP".equalsIgnoreCase(sp.getProtocol()) && sp.getEnabled() != 2) {
                UserServiceProvider assignment = new UserServiceProvider(user, sp);
                userServiceProviderRepository.save(assignment);
            }
        }
    }

    /**
     * Creates the root user if it doesn't already exist.
     */
    public void createRootUser() {
        Optional<Users> optionalUser = userRepository.findByUserName(appProperties.getRootUser());
        if (optionalUser.isEmpty()) {
            boolean existsRootUser = userRepository.existsByRolesContaining(UserRole.ROOT.getDisplayName());
            if (existsRootUser) {
                log.warn("There is already a root user assigning");
                return;
            }
            Users user = new Users();
            user.setName("ROOT");
            user.setLastName("ROOT");
            user.setRoles(new ArrayList<>(Collections.singletonList(UserRole.ROOT.getDisplayName())));
            user.setUserName(appProperties.getRootUser());
            user.setPassword(UtilsUser.encodePassword(appProperties.getRootPassword()));
            user.setStatus((short) 1);
            userRepository.save(user);
        }
    }

    private List<String> sanitizeSenderIds(List<String> rawSenderIds) {
        if (rawSenderIds == null || rawSenderIds.isEmpty()) {
            return new ArrayList<>();
        }
        return rawSenderIds.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .toList();
    }
}
