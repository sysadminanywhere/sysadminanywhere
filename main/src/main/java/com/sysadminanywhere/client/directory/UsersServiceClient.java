package com.sysadminanywhere.client.directory;

import com.sysadminanywhere.common.directory.model.UserEntry;
import com.sysadminanywhere.config.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(
        name = "users",
        url = "${app.services.directory.uri}",
        configuration = FeignClientConfig.class
)
public interface UsersServiceClient {

    @GetMapping("/api/users")
    Page<UserEntry> getAll(Pageable pageable, @RequestParam("filters") String filters);

    @GetMapping("/api/users/list")
    List<UserEntry> getList(@RequestParam("filters") String filters);

}