package com.sysadminanywhere.client.directory;

import com.sysadminanywhere.common.directory.dto.AddGroupDto;
import com.sysadminanywhere.common.directory.model.GroupEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;

import java.util.List;

public interface GroupsServiceClient {

    @GetExchange("/api/groups")
    Page<GroupEntry> getAll(Pageable pageable, String filters, String[] attributes);

    @GetExchange("/api/groups/list")
    List<GroupEntry> getList(String filters, String[] attributes);

    @GetExchange("/api/groups/{cn}")
    GroupEntry getByCN(String cn);

    @PostExchange("/api/groups")
    GroupEntry add(AddGroupDto addGroup);

    @PutExchange("/api/groups")
    GroupEntry update(GroupEntry group);

    @DeleteExchange("/api/groups")
    void delete(String distinguishedName);

}
