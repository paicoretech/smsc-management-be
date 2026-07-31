package com.smsc.management.app.user.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthRequestDTO {
    private String userName;
    private String password;
}
