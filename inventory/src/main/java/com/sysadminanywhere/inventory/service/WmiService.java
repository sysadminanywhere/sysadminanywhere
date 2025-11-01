package com.sysadminanywhere.inventory.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sysadminanywhere.common.rabbit.RequestMessage;
import com.sysadminanywhere.common.wmi.dto.ExecuteDto;
import com.sysadminanywhere.inventory.config.MessageConfig;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class WmiService {

    private final RabbitTemplate rabbitTemplate;
    private final DirectExchange exchange;

    public WmiService(RabbitTemplate rabbitTemplate, DirectExchange exchange) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchange = exchange;
    }

    List<Map<String, Object>> execute(ExecuteDto executeDto){

        try {
            RequestMessage message = new RequestMessage();
            message.setAction("wmi.execute");
            message.setData(executeDto);
            message.setServiceName("directory");
            message.setTimestamp(LocalDateTime.now());
            message.setId(UUID.randomUUID().toString());

            var response = rabbitTemplate.convertSendAndReceive(exchange.getName(), MessageConfig.routingKey, message);
            if (response != null) {
                ObjectMapper om = new ObjectMapper();
                String json = new String((byte[]) response);
                return List.of(om.readValue(json, Map[].class));
            }

        } catch (Exception e) {
        }

        return null;

    }

}
