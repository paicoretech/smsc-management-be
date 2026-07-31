package com.smsc.management.app.user.controller;

import com.smsc.management.app.user.dto.UserRoleDto;
import com.smsc.management.app.user.dto.UsersDTO;
import com.smsc.management.app.user.service.UserService;
import com.smsc.management.utils.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/user")
@PreAuthorize("hasAnyRole('ROOT', 'ADMINISTRATOR')")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/roles")
    public List<UserRoleDto> getAllRoles() {
        return userService.getAllRoles();
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getUsers(){
        ApiResponse result = userService.getAllUsers();
        return ResponseEntity.status(result.status()).body(result);
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse> createUser(@RequestBody @Valid UsersDTO user) {
        ApiResponse result = userService.createUser(user);
        return ResponseEntity.status(result.status()).body(result);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse> updateUser(@PathVariable("id") int id, @RequestBody @Valid UsersDTO user) {
        ApiResponse result = userService.updateUser(id, user);
        return ResponseEntity.status(result.status()).body(result);
    }
}
