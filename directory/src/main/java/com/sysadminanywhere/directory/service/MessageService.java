package com.sysadminanywhere.directory.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sysadminanywhere.common.directory.dto.SearchDto;
import com.sysadminanywhere.common.message.RequestMessage;
import com.sysadminanywhere.common.message.ResponseMessage;
import com.sysadminanywhere.common.wmi.dto.ExecuteDto;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.api.ldap.model.name.Dn;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MessageService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final LdapService ldapService;
    private final WmiService wmiService;

    public MessageService(KafkaTemplate<String, String> kafkaTemplate,
                          LdapService ldapService,
                          WmiService wmiService) {

        this.kafkaTemplate = kafkaTemplate;
        this.ldapService = ldapService;
        this.wmiService = wmiService;
    }

    @SneakyThrows
    @KafkaListener(topics = "directory-request", groupId = "g1")
    private void handleRequest(String message) {
        ObjectMapper mapper = new ObjectMapper();

        RequestMessage request = mapper.readValue(message, RequestMessage.class);

        Object result = null;

        switch (request.getAction()) {
            case "ldap.search":
                if (request.getData() != null) {
                    SearchDto searchDto = mapper.convertValue(request.getData(), SearchDto.class);

                    Dn dn = searchDto.getDistinguishedName().isEmpty() ? ldapService.getBaseDn() : new Dn(searchDto.getDistinguishedName());

                    String filter = searchDto.getFilter();
                    SearchScope searchScope = SearchScope.getSearchScope(searchDto.getSearchScope());
                    String[] attributes = new String[]{"*"};
                    if (searchDto.getAttributes() != null)
                        attributes = searchDto.getAttributes();

                    result = ldapService.convertEntryList(ldapService.searchWithAttributes(dn, filter, searchScope, attributes));
                }
                break;

            case "wmi.execute":
                if (request.getData() != null) {
                    ExecuteDto executeDto = mapper.convertValue(request.getData(), ExecuteDto.class);

                    result = wmiService.execute(executeDto.getHostName(), executeDto.getWqlQuery());
                }
                break;

            default:
                throw new IllegalArgumentException("Unknown action: " + request.getAction());
        }

        ResponseMessage responseMessage = new ResponseMessage(
                request.getId(),
                "SUCCESS",
                "Request processed successfully",
                result);

        kafkaTemplate.send("directory-response", mapper.writeValueAsString(responseMessage));
    }

}