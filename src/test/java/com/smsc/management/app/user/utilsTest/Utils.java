package com.smsc.management.app.user.utilsTest;

import com.smsc.management.app.user.dto.UsersDTO;
import com.smsc.management.utils.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class Utils {
    public static void checkUsersAssertions(ResponseEntity<ApiResponse> response, HttpStatus httpStatus) {
        assertNotNull(response);
        assertInstanceOf(ApiResponse.class, response.getBody());
        ApiResponse apiResponse = response.getBody();

        switch (httpStatus) {
            case OK -> {
                assertEquals(HttpStatus.OK, response.getStatusCode());
                assertEquals("success", response.getBody().message());
                assertNotNull(Objects.requireNonNull(apiResponse).data());
            }
            case NOT_FOUND -> {
                assertNull(Objects.requireNonNull(apiResponse).data());
                assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
                assertEquals("error", response.getBody().message());
            }
            case BAD_REQUEST -> {
                assertNull(Objects.requireNonNull(apiResponse).data());
                assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                assertEquals("error", response.getBody().message());
            }
            case INTERNAL_SERVER_ERROR -> {
                assertNull(Objects.requireNonNull(apiResponse).data());
                assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
                assertNull(response.getBody().data());
                assertEquals("error", response.getBody().message());
            }
            default -> throw new IllegalStateException("Unexpected value: " + response.getStatusCode());
        }
    }

    public static UsersDTO getUsersDTO(String password) {
        UsersDTO usersDTO = new UsersDTO();
        usersDTO.setUserName("test");
        usersDTO.setName("this a test");
        usersDTO.setLastName("test 2");
        usersDTO.setPassword(password.equals("null") ? null : password);
        usersDTO.setRoles(List.of("ADMINISTRATOR"));

        return usersDTO;
    }
}
