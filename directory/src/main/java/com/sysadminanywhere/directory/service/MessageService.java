package com.sysadminanywhere.directory.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sysadminanywhere.common.directory.dto.SearchDto;
import com.sysadminanywhere.common.rabbit.RequestMessage;
import com.sysadminanywhere.common.rabbit.ResponseMessage;
import com.sysadminanywhere.common.wmi.dto.ExecuteDto;
import com.sysadminanywhere.directory.config.MessageConfig;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.api.ldap.model.name.Dn;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
public class MessageService {

    private final RabbitTemplate rabbitTemplate;

    private final LdapService ldapService;
    private final WmiService wmiService;

    public MessageService(RabbitTemplate rabbitTemplate,
                          LdapService ldapService,
                          WmiService wmiService) {

        this.rabbitTemplate = rabbitTemplate;
        this.ldapService = ldapService;
        this.wmiService = wmiService;
    }

    @SneakyThrows
    @RabbitListener(queues = MessageConfig.queueName)
    private ResponseMessage handleRequest(RequestMessage request) {

        Object result = null;

        switch (request.getAction()) {
            case "ldap.search":
                if (request.getData() != null) {
                    ObjectMapper mapper = new ObjectMapper();
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
                    ObjectMapper mapper = new ObjectMapper();
                    ExecuteDto executeDto = mapper.convertValue(request.getData(), ExecuteDto.class);

                    result = wmiService.execute(executeDto.getHostName(), executeDto.getWqlQuery());
                }
                break;

            default:
                throw new IllegalArgumentException("Unknown action: " + request.getAction());
        }

        return new ResponseMessage(
                request.getId(),
                "SUCCESS",
                "Request processed successfully",
                result,
                LocalDateTime.now()
        );
    }

}