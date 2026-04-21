package com.sysadminanywhere.client.directory;

import com.sysadminanywhere.common.directory.model.PrinterEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;

import java.util.List;

public interface PrintersServiceClient {

    @GetExchange("/api/printers")
    Page<PrinterEntry> getAll(Pageable pageable, String filters, String[] attributes);

    @GetExchange("/api/printers/list")
    List<PrinterEntry> getList(String filters, String[] attributes);

    @GetExchange("/api/printers/{cn}")
    PrinterEntry getByCN(String cn);

    @DeleteExchange("/api/printers")
    void delete(String distinguishedName);

}
