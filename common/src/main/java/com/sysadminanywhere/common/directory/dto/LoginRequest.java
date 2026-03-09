package com.sysadminanywhere.common.directory.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LoginRequest {

    private String username;
    private String password;
    private String service;

    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public LoginRequest(String username, String password, String service) {
        this.username = username;
        this.password = password;
        this.service = service;
    }

}
