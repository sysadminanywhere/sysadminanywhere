package com.sysadminanywhere.client.directory;

import com.sysadminanywhere.common.PageResponse;
import com.sysadminanywhere.common.directory.dto.AddGroupDto;
import com.sysadminanywhere.common.directory.model.GroupEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;

import java.util.List;

public interface GroupsServiceClient {

    @GetExchange("/api/groups")
    PageResponse<GroupEntry> getAll(@RequestParam int page, @RequestParam int size, @RequestParam String sort, @RequestParam String filters, @RequestParam String[] attributes);

    @GetExchange("/api/groups/list")
    List<GroupEntry> getList(@RequestParam String filters, @RequestParam String[] attributes);

    @GetExchange("/api/groups/{cn}")
    GroupEntry getByCN(@PathVariable String cn);

    @PostExchange("/api/groups")
    GroupEntry add(@RequestBody AddGroupDto addGroup);

    @PutExchange("/api/groups")
    GroupEntry update(@RequestBody GroupEntry group);

    @DeleteExchange("/api/groups")
    void delete(@RequestParam String distinguishedName);

}
