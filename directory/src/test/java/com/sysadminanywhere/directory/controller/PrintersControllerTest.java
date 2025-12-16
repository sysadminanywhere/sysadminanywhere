package com.sysadminanywhere.directory.controller;

import com.sysadminanywhere.common.directory.model.PrinterEntry;
import com.sysadminanywhere.directory.service.PrintersService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PrintersControllerTest {

    @Mock
    private PrintersService printersService;

    @InjectMocks
    private PrintersController printersController;

    @Test
    void getAll_returnsPageOfPrinters() {
        PrinterEntry printer = createPrinterEntry("Printer01");
        Pageable pageable = PageRequest.of(0, 10);
        String[] attributes = {"cn"};
        Page<PrinterEntry> page = new PageImpl<>(List.of(printer));
        when(printersService.getAll(pageable, "", attributes)).thenReturn(page);

        var response = printersController.getAll(pageable, "", attributes);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isSameAs(page);
        verify(printersService).getAll(pageable, "", attributes);
    }

    @Test
    void getList_returnsListOfPrinters() {
        PrinterEntry printer = createPrinterEntry("Printer01");
        String[] attributes = {"cn"};
        when(printersService.getAll("", attributes)).thenReturn(List.of(printer));

        var response = printersController.getList("", attributes);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).containsExactly(printer);
        verify(printersService).getAll(eq(""), same(attributes));
    }

    @Test
    void getByCN_returnsPrinter() {
        PrinterEntry printer = createPrinterEntry("Printer01");
        when(printersService.getByCN("Printer01")).thenReturn(printer);

        var response = printersController.getByCN("Printer01");

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isSameAs(printer);
        verify(printersService).getByCN("Printer01");
    }

    @Test
    void delete_removesPrinter() {
        doNothing().when(printersService).delete(anyString());

        var response = printersController.delete("cn=Printer01,CN=Users,DC=example,DC=com");

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        verify(printersService).delete("cn=Printer01,CN=Users,DC=example,DC=com");
    }

    private PrinterEntry createPrinterEntry(String cn) {
        PrinterEntry printer = new PrinterEntry();
        printer.setCn(cn);
        printer.setDistinguishedName("cn=" + cn + ",CN=Users,DC=example,DC=com");
        return printer;
    }
}
