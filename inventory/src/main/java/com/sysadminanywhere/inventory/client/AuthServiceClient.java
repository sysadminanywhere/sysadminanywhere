package com.sysadminanywhere.inventory.client;

import com.sysadminanywhere.common.directory.dto.JwtResponse;
import com.sysadminanywhere.common.directory.dto.LoginRequest;
import org.springframework.web.service.annotation.PostExchange;

public interface AuthServiceClient {

    @PostExchange("/api/ldap/authenticate")
    JwtResponse authenticate(LoginRequest request);

}
