package com.sysadminanywhere.directory.controller;

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
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContactsControllerTest {

    @Mock
    private ContactsService contactsService;

    @InjectMocks
    private ContactsController contactsController;

    @Test
    void getAll_returnsPageOfContacts() {
        ContactEntry contact = createContactEntry("John Doe");

        Pageable pageable = PageRequest.of(0, 10);
        String[] attributes = {"cn"};
        Page<ContactEntry> page = new PageImpl<>(List.of(contact));
        when(contactsService.getAll(pageable, "", attributes)).thenReturn(page);

        var response = contactsController.getAll(pageable, "", attributes);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isSameAs(page);
        verify(contactsService).getAll(pageable, "", attributes);
    }

    @Test
    void getList_returnsListOfContacts() {
        ContactEntry contact = createContactEntry("John Doe");

        String[] attributes = {"cn"};
        when(contactsService.getAll("", attributes)).thenReturn(List.of(contact));

        var response = contactsController.getList("", attributes);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).containsExactly(contact);
        verify(contactsService).getAll(eq(""), same(attributes));
    }

    @Test
    void getByCN_returnsContact() {
        ContactEntry contact = createContactEntry("John Doe");

        when(contactsService.getByCN("John Doe")).thenReturn(contact);

        var response = contactsController.getByCN("John Doe");

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isSameAs(contact);
        verify(contactsService).getByCN("John Doe");
    }

    @Test
    void add_createsContact() {
        AddContactDto addContactDto = new AddContactDto();
        addContactDto.setCn("John Doe");
        addContactDto.setDisplayName("John Doe");
        addContactDto.setFirstName("John");
        addContactDto.setLastName("Doe");
        addContactDto.setInitials("JD");

        ContactEntry createdContact = createContactEntry("John Doe");

        when(contactsService.add(isNull(), eq("John Doe"), eq("John Doe"), eq("John"), eq("Doe"), eq("JD")))
                .thenReturn(createdContact);

        var response = contactsController.add(addContactDto);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isSameAs(createdContact);
        verify(contactsService).add(isNull(), eq("John Doe"), eq("John Doe"), eq("John"), eq("Doe"), eq("JD"));
    }

    @Test
    void update_modifiesContact() {
        ContactEntry contact = createContactEntry("John Doe");

        when(contactsService.update(any(ContactEntry.class))).thenReturn(contact);

        var response = contactsController.update(contact);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isSameAs(contact);
        verify(contactsService).update(any(ContactEntry.class));
    }

    @Test
    void delete_removesContact() {
        doNothing().when(contactsService).delete(anyString());

        var response = contactsController.delete("cn=John Doe,CN=Users,DC=example,DC=com");

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        verify(contactsService).delete("cn=John Doe,CN=Users,DC=example,DC=com");
    }

    private ContactEntry createContactEntry(String cn) {
        ContactEntry contact = new ContactEntry();
        contact.setCn(cn);
        contact.setDistinguishedName("cn=" + cn + ",CN=Users,DC=example,DC=com");
        return contact;
    }
}
