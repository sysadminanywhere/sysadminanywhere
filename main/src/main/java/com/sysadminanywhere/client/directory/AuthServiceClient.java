package com.sysadminanywhere.client.directory;

import com.sysadminanywhere.common.directory.dto.LoginRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import com.sysadminanywhere.common.directory.dto.JwtResponse;

@FeignClient(
        name = "auth",
        url = "${app.services.directory.uri}"
)
public interface AuthServiceClient {

    @PostMapping("/api/ldap/authenticate")
    JwtResponse authenticate(@RequestBody LoginRequest request);;

}