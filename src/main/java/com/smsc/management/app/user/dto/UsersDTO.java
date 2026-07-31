package com.smsc.management.app.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UsersDTO {
    private Integer id;

    @NotNull(message = "name cannot be empty")
    @NotBlank(message = "name cannot be empty")
    private String name;

    @JsonProperty("last_name")
    private String lastName;

    @NotNull(message = "name cannot be empty")
    @NotBlank(message = "name cannot be empty")
    @JsonProperty("user_name")
    private String userName;

    @JsonProperty("sender_ids")
    private List<String> senderIds = new ArrayList<>();

    private String password;

    @NotNull(message = "roles cannot be null")
    @NotEmpty(message = "roles cannot be empty")
    private List<String> roles;

    private Short status = 1;

    @JsonProperty("account_locked")
    private boolean accountLocked = false;

    @JsonProperty("lock_time")
    private LocalDateTime lockTime;

    @JsonProperty("service_providers")
    private List<Integer> serviceProviders;

    @JsonProperty("all_service_providers")
    private boolean allServiceProviders;
}
