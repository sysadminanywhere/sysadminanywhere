package com.sysadminanywhere.api;

import com.sysadminanywhere.model.GroupEntry;
import com.sysadminanywhere.model.UserEntry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.net.CacheRequest;

@FeignClient(name = "localhost", url = "http://localhost:8081")
public interface DirectoryClient {

    @GetMapping("/api/users")
    public ResponseEntity<Page<UserEntry>> getAllUsers(Pageable pageable);

    @GetMapping("/api/groups")
    public ResponseEntity<Page<GroupEntry>> getAllGroups(Pageable pageable);

    @GetMapping("/api/me")
    public ResponseEntity<UserEntry> getMe();

    @GetMapping("/api/users/{cn}")
    public ResponseEntity<UserEntry> getByCN(@PathVariable("cn") String cn);

}
