package com.sysadminanywhere.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sysadminanywhere.service.WebhookService;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JacksonConfigurationTest {
    @Test
    void providesObjectMapperWithJavaTimeSupport() throws Exception {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.register(JacksonConfiguration.class, WebhookService.class);
            context.refresh();
            ObjectMapper mapper = context.getBean(ObjectMapper.class);
            context.getBean(WebhookService.class);
            assertEquals("\"2026-09-24T09:04:25\"",
                    mapper.writeValueAsString(LocalDateTime.of(2026, 9, 24, 9, 4, 25)));
        }
    }
}
