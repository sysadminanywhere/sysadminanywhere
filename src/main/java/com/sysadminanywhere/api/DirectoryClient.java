package com.sysadminanywhere.api;

import com.sysadminanywhere.model.GroupEntry;
import com.sysadminanywhere.model.UserEntry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.CacheRequest;

@FeignClient(name = "localhost", url = "http://localhost:8081")
public interface DirectoryClient {

    @GetMapping("/api/users")
    public ResponseEntity<Page<UserEntry>> getAllUsers(Pageable pageable);

    @GetMapping("/api/groups")
    public ResponseEntity<Page<GroupEntry>> getAllGroups(Pageable pageable);

    @GetMapping("/api/login")
    public ResponseEntity<Boolean> login(@RequestParam String userName, @RequestParam String password);

    @GetMapping("/api/users/{cn}")
    public ResponseEntity<UserEntry> getByCN(@PathVariable("cn") String cn);

}
