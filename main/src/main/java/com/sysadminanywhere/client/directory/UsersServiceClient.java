package com.sysadminanywhere.client.directory;

import com.sysadminanywhere.common.directory.model.UserEntry;
import com.sysadminanywhere.config.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "directory",
        url = "${app.services.directory.uri}",
        configuration = FeignClientConfig.class
)
public interface UsersServiceClient {

    @GetMapping("/api/users")
    Page<UserEntry> getUsers(Pageable pageable, @RequestParam("filters") String filters);

}