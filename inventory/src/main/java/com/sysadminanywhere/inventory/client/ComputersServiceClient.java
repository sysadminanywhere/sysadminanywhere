package com.sysadminanywhere.inventory.client;

import com.sysadminanywhere.common.directory.dto.AddComputerDto;
import com.sysadminanywhere.common.directory.model.ComputerEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;

import java.util.List;

public interface ComputersServiceClient {

    @GetExchange("/api/computers")
    Page<ComputerEntry> getAll(Pageable pageable, String filters, String[] attributes);

    @GetExchange("/api/computers/list")
    List<ComputerEntry> getList(String filters, String... attributes);

    @GetExchange("/api/computers/{cn}")
    ComputerEntry getByCN(String cn);

    @PostExchange("/api/computers")
    ComputerEntry add(AddComputerDto addComputer);

    @PutExchange("/api/computers")
    ComputerEntry update(ComputerEntry computer);

    @DeleteExchange("/api/computers")
    void delete(String distinguishedName);

}
