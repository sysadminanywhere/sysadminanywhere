package com.sysadminanywhere.inventory.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sysadminanywhere.common.directory.dto.EntryDto;
import com.sysadminanywhere.common.directory.dto.SearchDto;
import com.sysadminanywhere.common.rabbit.RequestMessage;
import com.sysadminanywhere.common.rabbit.ResponseMessage;
import com.sysadminanywhere.inventory.config.MessageConfig;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class LdapService {

    private final RabbitTemplate rabbitTemplate;
    private final DirectExchange exchange;

    public LdapService(RabbitTemplate rabbitTemplate, DirectExchange exchange) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchange = exchange;
    }

    List<EntryDto> getSearch(SearchDto searchDto) {

        try {
            RequestMessage message = new RequestMessage();
            message.setAction("ldap.search");
            message.setData(searchDto);
            message.setServiceName("directory");
            message.setTimestamp(LocalDateTime.now());
            message.setId(UUID.randomUUID().toString());

            var response = rabbitTemplate.convertSendAndReceive(exchange.getName(), MessageConfig.routingKey, message);
            if (response != null && response instanceof ResponseMessage) {
                ResponseMessage responseMessage = (ResponseMessage) response;
                return (List<EntryDto>) responseMessage.getResult();
            }

        } catch (Exception e) {
        }

        return null;
    }

}