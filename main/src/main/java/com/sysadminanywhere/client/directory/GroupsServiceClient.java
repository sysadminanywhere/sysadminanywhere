package com.sysadminanywhere.client.directory;

import com.sysadminanywhere.common.directory.dto.AddComputerDto;
import com.sysadminanywhere.common.directory.dto.AddGroupDto;
import com.sysadminanywhere.common.directory.model.GroupEntry;
import com.sysadminanywhere.config.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(
        name = "groups",
        url = "${app.services.directory.uri}",
        configuration = FeignClientConfig.class
)
public interface GroupsServiceClient {

    @GetMapping("/api/groups")
    Page<GroupEntry> getAll(Pageable pageable, @RequestParam("filters") String filters, @RequestParam("attributes") String[] attributes);

    @GetMapping("/api/groups/list")
    List<GroupEntry> getList(@RequestParam("filters") String filters);

    @GetMapping("/api/groups/{cn}")
    GroupEntry getByCN(@PathVariable("cn") String cn);

    @PostMapping("/api/groups")
    GroupEntry add(@RequestBody AddGroupDto addGroup);

    @PutMapping("/api/groups")
    GroupEntry update(@RequestBody GroupEntry group);

    @DeleteMapping("/api/groups")
    void delete(@RequestParam("distinguishedName") String distinguishedName);

}