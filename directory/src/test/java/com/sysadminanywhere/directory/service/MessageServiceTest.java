package com.sysadminanywhere.directory.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sysadminanywhere.common.directory.dto.EntryDto;
import com.sysadminanywhere.common.directory.dto.SearchDto;
import com.sysadminanywhere.common.wmi.dto.ExecuteDto;
import org.apache.directory.api.ldap.model.entry.DefaultEntry;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.api.ldap.model.name.Dn;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private LdapService ldapService;

    @Mock
    private WmiService wmiService;

    private ObjectMapper mapper;

    private MessageService messageService;

    @BeforeEach
    void setUp() throws Exception {
        mapper = new ObjectMapper();
        messageService = new MessageService(kafkaTemplate, ldapService, wmiService, mapper);
    }

    @Test
    void handleRequest_ignoresMessageWhenRecipientIsNotDirectory() throws Exception {
        Map<String, Object> headersMap = new HashMap<>();
        headersMap.put("action", "ldap.search");
        headersMap.put("sender", "other-service");
        headersMap.put("recipient", "other-service");
        headersMap.put("method", "GET");
        headersMap.put("correlationId", "123");
        MessageHeaders headers = new MessageHeaders(headersMap);

        java.lang.reflect.Method method = MessageService.class.getDeclaredMethod("handleRequest", MessageHeaders.class, String.class);
        method.setAccessible(true);
        method.invoke(messageService, headers, "{}");

        verify(kafkaTemplate, never()).send(any(Message.class));
    }

    @Test
    void handleRequest_processesLdapSearchAction() throws Exception {
        Dn baseDn = new Dn("DC=example,DC=com");
        when(ldapService.getBaseDn()).thenReturn(baseDn);

        Entry entry = new DefaultEntry("cn=test,dc=example,dc=com");
        entry.add("cn", "test");

        EntryDto entryDto = new EntryDto();
        entryDto.setDn("cn=test,dc=example,dc=com");
        entryDto.setAttributes(Map.of("cn", "test"));

        when(ldapService.searchWithAttributes(any(Dn.class), anyString(), any(SearchScope.class), any(String[].class)))
                .thenReturn(List.of(entry));
        when(ldapService.convertEntryList(anyList())).thenReturn(List.of(entryDto));

        SearchDto searchDto = new SearchDto();
        searchDto.setDistinguishedName("");
        searchDto.setFilter("(objectClass=*)");
        searchDto.setSearchScope(2);

        Map<String, Object> headersMap = new HashMap<>();
        headersMap.put("action", "ldap.search");
        headersMap.put("sender", "other-service");
        headersMap.put("recipient", "directory");
        headersMap.put("method", "GET");
        headersMap.put("correlationId", "123");
        MessageHeaders headers = new MessageHeaders(headersMap);

        java.lang.reflect.Method method = MessageService.class.getDeclaredMethod("handleRequest", MessageHeaders.class, String.class);
        method.setAccessible(true);
        method.invoke(messageService, headers, mapper.writeValueAsString(searchDto));

        verify(kafkaTemplate).send(any(Message.class));
    }

    @Test
    void handleRequest_processesWmiExecuteAction() throws Exception {
        List<Map<String, Object>> wmiResult = List.of(Map.of("Name", "TestProcess"));
        when(wmiService.execute(anyString(), anyString())).thenReturn(wmiResult);

        ExecuteDto executeDto = new ExecuteDto();
        executeDto.setHostName("server01.example.com");
        executeDto.setWqlQuery("SELECT * FROM Win32_Process");

        Map<String, Object> headersMap = new HashMap<>();
        headersMap.put("action", "wmi.execute");
        headersMap.put("sender", "other-service");
        headersMap.put("recipient", "directory");
        headersMap.put("method", "GET");
        headersMap.put("correlationId", "456");
        MessageHeaders headers = new MessageHeaders(headersMap);

        java.lang.reflect.Method method = MessageService.class.getDeclaredMethod("handleRequest", MessageHeaders.class, String.class);
        method.setAccessible(true);
        method.invoke(messageService, headers, mapper.writeValueAsString(executeDto));

        verify(wmiService).execute("server01.example.com", "SELECT * FROM Win32_Process");
        verify(kafkaTemplate).send(any(Message.class));
    }

    @Test
    void handleRequest_handlesUnknownAction() throws Exception {
        Map<String, Object> headersMap = new HashMap<>();
        headersMap.put("action", "unknown.action");
        headersMap.put("sender", "other-service");
        headersMap.put("recipient", "directory");
        headersMap.put("method", "GET");
        headersMap.put("correlationId", "789");
        MessageHeaders headers = new MessageHeaders(headersMap);

        java.lang.reflect.Method method = MessageService.class.getDeclaredMethod("handleRequest", MessageHeaders.class, String.class);
        method.setAccessible(true);
        method.invoke(messageService, headers, "{}");

        verify(kafkaTemplate, never()).send(any(Message.class));
    }

    @Test
    void handleRequest_usesSpecifiedDnWhenProvided() throws Exception {
        Dn specifiedDn = new Dn("OU=Users,DC=example,DC=com");

        Entry entry = new DefaultEntry("cn=test,ou=Users,dc=example,dc=com");
        entry.add("cn", "test");

        when(ldapService.searchWithAttributes(any(Dn.class), anyString(), any(SearchScope.class), any(String[].class)))
                .thenReturn(List.of(entry));
        when(ldapService.convertEntryList(anyList())).thenReturn(Collections.emptyList());

        SearchDto searchDto = new SearchDto();
        searchDto.setDistinguishedName("OU=Users,DC=example,DC=com");
        searchDto.setFilter("(objectClass=user)");
        searchDto.setSearchScope(2);
        searchDto.setAttributes(new String[]{"cn", "sn"});

        Map<String, Object> headersMap = new HashMap<>();
        headersMap.put("action", "ldap.search");
        headersMap.put("sender", "other-service");
        headersMap.put("recipient", "directory");
        headersMap.put("method", "GET");
        headersMap.put("correlationId", "123");
        MessageHeaders headers = new MessageHeaders(headersMap);

        java.lang.reflect.Method method = MessageService.class.getDeclaredMethod("handleRequest", MessageHeaders.class, String.class);
        method.setAccessible(true);
        method.invoke(messageService, headers, mapper.writeValueAsString(searchDto));

        ArgumentCaptor<Dn> dnCaptor = ArgumentCaptor.forClass(Dn.class);
        verify(ldapService).searchWithAttributes(dnCaptor.capture(), eq("(objectClass=user)"), eq(SearchScope.SUBTREE), any(String[].class));
        assertThat(dnCaptor.getValue().getName()).isEqualTo("OU=Users,DC=example,DC=com");
    }
}
