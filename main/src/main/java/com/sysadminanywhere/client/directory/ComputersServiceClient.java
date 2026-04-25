package com.sysadminanywhere.client.directory;

import com.sysadminanywhere.common.PageResponse;
import com.sysadminanywhere.common.directory.dto.AddComputerDto;
import com.sysadminanywhere.common.directory.model.ComputerEntry;
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

public interface ComputersServiceClient {

    @GetExchange("/api/computers")
    PageResponse<ComputerEntry> getAll(@RequestParam int page, @RequestParam int size, @RequestParam String sort, @RequestParam String filters, @RequestParam String[] attributes);

    @GetExchange("/api/computers/list")
    List<ComputerEntry> getList(@RequestParam String filters, @RequestParam String... attributes);

    @GetExchange("/api/computers/{cn}")
    ComputerEntry getByCN(@PathVariable String cn);

    @PostExchange("/api/computers")
    ComputerEntry add(@RequestBody AddComputerDto addComputer);

    @PutExchange("/api/computers")
    ComputerEntry update(@RequestBody ComputerEntry computer);

    @DeleteExchange("/api/computers")
    void delete(@RequestParam String distinguishedName);

}
