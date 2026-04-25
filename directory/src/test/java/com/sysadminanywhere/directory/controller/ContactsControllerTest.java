package com.sysadminanywhere.directory.controller;

import com.sysadminanywhere.common.PageResponse;
import com.sysadminanywhere.common.directory.dto.AddContactDto;
import com.sysadminanywhere.common.directory.model.ContactEntry;
import com.sysadminanywhere.directory.service.ContactsService;
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
class ContactsControllerTest {

    @Mock
    private ContactsService contactsService;

    @InjectMocks
    private ContactsController contactsController;

    @Test
    void getAll_shouldReturnPageOfContacts() {
        Page<ContactEntry> page = new PageImpl<>(List.of(new ContactEntry()));
        when(contactsService.getAll(any(PageRequest.class), anyString(), any(String[].class))).thenReturn(page);

        ResponseEntity<PageResponse<ContactEntry>> result = contactsController.getAll(0, 10, "", "filter", new String[]{"cn"});

        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(contactsService).getAll(any(PageRequest.class), eq("filter"), any(String[].class));
    }

    @Test
    void getList_shouldReturnListOfContacts() {
        List<ContactEntry> contacts = List.of(new ContactEntry(), new ContactEntry());
        when(contactsService.getAll(anyString(), any(String[].class))).thenReturn(contacts);

        ResponseEntity<List<ContactEntry>> result = contactsController.getList("", new String[]{"cn"});

        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(2, result.getBody().size());
    }

    @Test
    void getByCN_shouldReturnContact() {
        ContactEntry contact = new ContactEntry();
        contact.setCn("John Doe");
        when(contactsService.getByCN("John Doe")).thenReturn(contact);

        ResponseEntity<ContactEntry> result = contactsController.getByCN("John Doe");

        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("John Doe", result.getBody().getCn());
    }

    @Test
    void add_shouldCreateContact() {
        AddContactDto dto = new AddContactDto();
        dto.setDistinguishedName("CN=Jane Doe,OU=Contacts,DC=com");
        dto.setCn("Jane Doe");
        dto.setFirstName("Jane");
        dto.setLastName("Doe");

        ContactEntry contact = new ContactEntry();
        contact.setCn("Jane Doe");
        // Use doReturn().when() with any() matchers to avoid strict stubbing
        ContactEntry mockResult = new ContactEntry();
        mockResult.setCn("Jane Doe");
        lenient().when(contactsService.add(anyString(), anyString(), any(), anyString(), anyString(), any()))
            .thenReturn(mockResult);

        ResponseEntity<ContactEntry> result = contactsController.add(dto);

        assertNotNull(result);
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
    }

    @Test
    void delete_shouldCallServiceDelete() {
        String dn = "CN=Contact,OU=Contacts,DC=example,DC=com";
        doNothing().when(contactsService).delete(dn);

        ResponseEntity<?> result = contactsController.delete(dn);

        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        verify(contactsService).delete(dn);
    }
}
