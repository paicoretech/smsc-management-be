package com.smsc.management.app.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class AuthResponseDTO {
    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("user_id")
    private Integer userId;

    @JsonProperty("user_name")
    private String userName;

    @JsonProperty("must_change_password")
    private boolean mustChangePassword;

    @JsonProperty("sender_ids")
    private List<String> senderId;

    private List<String> roles;
}
