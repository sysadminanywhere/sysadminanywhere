package com.sysadminanywhere.client.directory;

import com.sysadminanywhere.common.PageResponse;
import com.sysadminanywhere.common.directory.model.PrinterEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;

import java.util.List;

public interface PrintersServiceClient {

    @GetExchange("/api/printers")
    PageResponse<PrinterEntry> getAll(@RequestParam int page, @RequestParam int size, @RequestParam String sort, @RequestParam String filters, @RequestParam String[] attributes);

    @GetExchange("/api/printers/list")
    List<PrinterEntry> getList(@RequestParam String filters, @RequestParam String[] attributes);

    @GetExchange("/api/printers/{cn}")
    PrinterEntry getByCN(@PathVariable String cn);

    @DeleteExchange("/api/printers")
    void delete(@RequestParam String distinguishedName);

}
