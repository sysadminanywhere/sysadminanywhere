package com.sysadminanywhere.directory.controller;

import com.sysadminanywhere.common.PageResponse;
import com.sysadminanywhere.common.directory.dto.*;
import com.sysadminanywhere.directory.service.LdapService;
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
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LdapControllerTest {

    @Mock
    private LdapService ldapService;

    @InjectMocks
    private LdapController ldapController;

    @Test
    void getAudit_shouldReturnPageOfAudits() {
        PageResponse<AuditDto> pageResponse = new PageResponse<>(List.of(new AuditDto()), 0, 10, 1, 1);
        when(ldapService.getAudit(any(PageRequest.class), anyMap())).thenReturn(pageResponse);

        ResponseEntity<PageResponse<AuditDto>> result = ldapController.getAudit(0, 10, "", Map.of());

        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(ldapService).getAudit(any(PageRequest.class), anyMap());
    }

    @Test
    void getAuditList_shouldReturnListOfAudits() {
        List<AuditDto> audits = List.of(new AuditDto(), new AuditDto());
        when(ldapService.getAuditList(anyMap())).thenReturn(audits);

        ResponseEntity<List<AuditDto>> result = ldapController.getAuditList(Map.of());

        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(2, result.getBody().size());
    }

    @Test
    void getRootDse_shouldReturnRootDse() {
        EntryDto entryDto = new EntryDto();
        entryDto.setDn("DC=com");
        when(ldapService.getDomainEntry()).thenReturn(null);
        when(ldapService.convertEntry(any())).thenReturn(entryDto);

        ResponseEntity<EntryDto> result = ldapController.getRootDse();

        assertNotNull(result);
        verify(ldapService).getDomainEntry();
    }

    @Test
    void addMember_shouldCallServiceAddMember() {
        when(ldapService.addMember(anyString(), anyString())).thenReturn(true);

        ResponseEntity<?> result = ldapController.addMember("CN=User,DC=com", "CN=Group,DC=com");

        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(ldapService).addMember("CN=User,DC=com", "CN=Group,DC=com");
    }

    @Test
    void deleteMember_shouldCallServiceDeleteMember() {
        when(ldapService.deleteMember(anyString(), anyString())).thenReturn(true);

        ResponseEntity<?> result = ldapController.deleteMember("CN=User,DC=com", "CN=Group,DC=com");

        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(ldapService).deleteMember("CN=User,DC=com", "CN=Group,DC=com");
    }

    @Test
    void authenticate_shouldReturnJwtResponse() {
        LoginRequest request = new LoginRequest("admin", "password");
        JwtResponse jwtResponse = new JwtResponse("token", "admin", List.of("ROLE_ADMIN"));
        when(ldapService.authenticate("admin", "password", null)).thenReturn(jwtResponse);

        ResponseEntity<?> result = ldapController.authenticate(request);

        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(ldapService).authenticate("admin", "password", null);
    }

    @Test
    void authenticate_shouldReturnUnauthorizedForNullCredentials() {
        ResponseEntity<?> result = ldapController.authenticate(null);

        assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
        verifyNoInteractions(ldapService);
    }
}
