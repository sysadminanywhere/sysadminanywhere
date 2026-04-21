package com.sysadminanywhere.client.directory;

import com.sysadminanywhere.common.directory.dto.JwtResponse;
import com.sysadminanywhere.common.directory.dto.LoginRequest;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.PostExchange;

public interface AuthServiceClient {

    @PostExchange("/api/ldap/authenticate")
    JwtResponse authenticate(@RequestBody LoginRequest request);

}
