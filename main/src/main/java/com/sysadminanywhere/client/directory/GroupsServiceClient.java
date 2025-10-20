package com.sysadminanywhere.client.directory;

import com.sysadminanywhere.common.directory.model.ComputerEntry;
import com.sysadminanywhere.common.directory.model.GroupEntry;
import com.sysadminanywhere.config.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(
        name = "groups",
        url = "${app.services.directory.uri}",
        configuration = FeignClientConfig.class
)
public interface GroupsServiceClient {

    @GetMapping("/api/groups")
    Page<GroupEntry> getAll(Pageable pageable, @RequestParam("filters") String filters);

    @GetMapping("/api/groups/list")
    List<GroupEntry> getList(@RequestParam("filters") String filters);

}