package com.sysadminanywhere.incident.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class TicketNumberGenerator {

    @Value("${app.ticket.number.template:T-{timestamp}}")
    private String template;

    public String generate() {
        String result = template;
        
        long timestamp = System.currentTimeMillis();
        LocalDateTime now = LocalDateTime.now();
        
        result = result.replace("{timestamp}", String.valueOf(timestamp));
        result = result.replace("{date}", now.format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        result = result.replace("{time}", now.format(DateTimeFormatter.ofPattern("HHmmss")));
        result = result.replace("{datetime}", now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        
        return result;
    }
}
