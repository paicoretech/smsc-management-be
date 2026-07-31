package com.smsc.management.app.user.controller;

import com.smsc.management.app.user.dto.UserRoleDto;
import com.smsc.management.app.user.dto.UsersDTO;
import com.smsc.management.app.user.utilsTest.Utils;
import com.smsc.management.integration.BaseIntegrationTest;
import com.smsc.management.utils.ApiResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.List;
import java.util.Objects;

import static com.smsc.management.app.ss7.utilsTest.Utils.checkAssertions;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserControllerTest extends BaseIntegrationTest {
    @Autowired
    private UserController userController;

    @WithMockUser(roles = {"ROOT"})
    @Test
    @DisplayName("Get all roles")
    void getAllRolesWhenDataIsOKThenDoItSuccessfully() {
        List<UserRoleDto> response = userController.getAllRoles();
        boolean existsRootRole = false;
        boolean existsAdminRole = false;
        for (UserRoleDto role : response) {
            if (role.getName().equals("ROOT")){
                existsRootRole = true;
            }

            if (role.getName().equals("ADMINISTRATOR")){
                existsAdminRole = true;
            }
        }
        assertTrue(existsAdminRole);
        assertFalse(existsRootRole);
        assertNotNull(response);
    }

    @Test
    @WithMockUser(roles = {"ADMINISTRATOR"})
    @DisplayName("Get all roles when profile is ADMINISTRATOR when ACTIONS_ADVANCED not is present")
    void getAllRolesWhenProfileIsAdministratorThenDoItSuccessfully() {
        List<UserRoleDto> response = userController.getAllRoles();
        boolean existsActionAdvancedRole = false;
        for (UserRoleDto role : response) {
            if (role.getName().equals("ACTION_ADVANCED")) {
                existsActionAdvancedRole = true;
                break;
            }
        }
        assertFalse(existsActionAdvancedRole);
        assertNotNull(response);
    }

    @Test
    @WithMockUser(roles = {"ADMINISTRATOR"})
    @DisplayName("Get all users data")
    @SuppressWarnings("unchecked")
    void getUsersTestWhenDataIsNotEmptyThenDoItSuccessfully() {
        ResponseEntity<ApiResponse> response = userController.getUsers();
        assertTrue(response.getStatusCode().is2xxSuccessful());

        ApiResponse apiResponse = response.getBody();
        List<UsersDTO>  data = (List<UsersDTO>) Objects.requireNonNull(apiResponse).data();
        assertFalse(data.isEmpty());
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    @DisplayName("Create new user when data is ok and password is invalid")
    void createUserTestWithDifferentPasswordValueThenDoItSuccessfully(String password) {
        UsersDTO usersDTO = Utils.getUsersDTO(password);

        ResponseEntity<ApiResponse> response = userController.createUser(usersDTO);
        assertFalse(response.getStatusCode().is2xxSuccessful());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @ParameterizedTest
    @ValueSource(ints = {1, 2})
    @DisplayName("Update user when user exists and data is ok")
    @SuppressWarnings("unchecked")
    void updateUserTestWhenDataIsOKThenDoItSuccessfully(int userId) {
        UsersDTO usersDTO = Utils.getUsersDTO("passwordTest");
        ResponseEntity<ApiResponse> response = userController.createUser(usersDTO);
        Utils.checkUsersAssertions(response, HttpStatus.OK);
        ApiResponse apiResponse = response.getBody();
        assertNotNull(apiResponse);
        UsersDTO newUser = (UsersDTO) Objects.requireNonNull(apiResponse).data();

        if (userId == 1) {
            userId = newUser.getId();
        }

        usersDTO.setUserName("newUsername");
        usersDTO.setPassword("newPassword");
        usersDTO.setRoles(List.of("ROOT"));
        response = userController.updateUser(userId, usersDTO);

        if (userId == newUser.getId()) {
            assertSame(HttpStatus.OK, response.getStatusCode());

            response = userController.getUsers();
            checkAssertions(response, HttpStatus.OK);
            ApiResponse apiResponseUpdate = response.getBody();
            List<UsersDTO>  usersList = (List<UsersDTO>) Objects.requireNonNull(apiResponseUpdate).data();
            UsersDTO userUpdated = new UsersDTO();
            for (UsersDTO user : usersList) {
                if (user.getId().equals(newUser.getId())) {
                    userUpdated = user;
                    break;
                }
            }
            assertNotNull(userUpdated);

            assertNotSame(userUpdated, newUser);
            assertEquals("newUsername", userUpdated.getUserName());
            assertNull(userUpdated.getPassword()); // in the get request the password is not present
            assertTrue(userUpdated.getRoles().contains("ROOT"));
        } else {
            checkAssertions(response, HttpStatus.NOT_FOUND);
        }
    }

    @Test
    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @DisplayName("Update user when user does not exist and optionally throws exception")
    void updateUserTestWhenUserNotExistsAndWithThrowExceptionThenGetHttpStatusError() {
        ResponseEntity<ApiResponse> response = userController.updateUser(1000, new UsersDTO());
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertFalse(response.getStatusCode().is2xxSuccessful());
    }
}