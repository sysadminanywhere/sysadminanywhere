package com.sysadminanywhere.directory.controller;

import com.sysadminanywhere.common.directory.model.PrinterEntry;
import com.sysadminanywhere.directory.service.PrintersService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PrintersControllerTest {

    @Mock
    private PrintersService printersService;

    @InjectMocks
    private PrintersController printersController;

    @Test
    void getAll_shouldReturnPageOfPrinters() {
        PageRequest pageable = PageRequest.of(0, 10);
        Page<PrinterEntry> page = new PageImpl<>(List.of(new PrinterEntry()));
        when(printersService.getAll(any(PageRequest.class), anyString(), any(String[].class))).thenReturn(page);

        ResponseEntity<Page<PrinterEntry>> result = printersController.getAll(pageable, "filter", new String[]{"cn"});

        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(printersService).getAll(eq(pageable), eq("filter"), any(String[].class));
    }

    @Test
    void getList_shouldReturnListOfPrinters() {
        List<PrinterEntry> printers = List.of(new PrinterEntry(), new PrinterEntry());
        when(printersService.getAll(anyString(), any(String[].class))).thenReturn(printers);

        ResponseEntity<List<PrinterEntry>> result = printersController.getList("", new String[]{"cn"});

        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(2, result.getBody().size());
    }

    @Test
    void getByCN_shouldReturnPrinter() {
        PrinterEntry printer = new PrinterEntry();
        printer.setCn("HP-LaserJet");
        when(printersService.getByCN("HP-LaserJet")).thenReturn(printer);

        ResponseEntity<PrinterEntry> result = printersController.getByCN("HP-LaserJet");

        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("HP-LaserJet", result.getBody().getCn());
    }

    @Test
    void delete_shouldCallServiceDelete() {
        String dn = "CN=Printer,OU=Printers,DC=example,DC=com";
        doNothing().when(printersService).delete(dn);

        ResponseEntity<?> result = printersController.delete(dn);

        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        verify(printersService).delete(dn);
    }
}
