package com.sysadminanywhere.directory.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sysadminanywhere.common.directory.dto.SearchDto;
import com.sysadminanywhere.common.wmi.dto.ExecuteDto;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.api.ldap.model.name.Dn;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MessageService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final LdapService ldapService;
    private final WmiService wmiService;
    private final ObjectMapper mapper;

    public MessageService(KafkaTemplate<String, String> kafkaTemplate,
                          LdapService ldapService,
                          WmiService wmiService, ObjectMapper mapper) {

        this.kafkaTemplate = kafkaTemplate;
        this.ldapService = ldapService;
        this.wmiService = wmiService;
        this.mapper = mapper;
    }

    @SneakyThrows
    @KafkaListener(topics = "directory-request", groupId = "directory")
    private void handleRequest(@Headers MessageHeaders headers, @Payload String message) {

        String action = headers.get("action").toString();
        String correlationId = headers.get("correlationId").toString();
        String sender = headers.get("sender").toString();
        String recipient = headers.get("recipient").toString();
        String method = headers.get("method").toString();

        if (!recipient.equalsIgnoreCase("directory"))
            return;

        Object result = null;

        switch (action) {
            case "ldap.search":
                SearchDto searchDto = mapper.readValue(message, SearchDto.class);

                Dn dn = searchDto.getDistinguishedName().isEmpty() ? ldapService.getBaseDn() : new Dn(searchDto.getDistinguishedName());

                String filter = searchDto.getFilter();
                SearchScope searchScope = SearchScope.getSearchScope(searchDto.getSearchScope());
                String[] attributes = new String[]{"*"};
                if (searchDto.getAttributes() != null)
                    attributes = searchDto.getAttributes();

                result = ldapService.convertEntryList(ldapService.searchWithAttributes(dn, filter, searchScope, attributes));
                break;

            case "wmi.execute":
                ExecuteDto executeDto = mapper.readValue(message, ExecuteDto.class);

                result = wmiService.execute(executeDto.getHostName(), executeDto.getWqlQuery());
                break;

            default:
                throw new IllegalArgumentException("Unknown action: " + action);
        }

        Message<String> kafkaMessage = MessageBuilder
                .withPayload(mapper.writeValueAsString(result))
                .setHeader(KafkaHeaders.TOPIC, "directory-response")
                .setHeader("correlationId", correlationId)
                .setHeader("action", action)
                .setHeader("sender", recipient)
                .setHeader("recipient", sender)
                .setHeader("method", method)
                .build();

        kafkaTemplate.send(kafkaMessage);
    }

}