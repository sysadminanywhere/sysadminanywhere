package com.sysadminanywhere.directory.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JacksonConfigurationTest {
    @Test
    void registersMapperForDirectoryJsonServices() throws Exception {
        try (AnnotationConfigApplicationContext context =
                     new AnnotationConfigApplicationContext(JacksonConfiguration.class)) {
            assertEquals("\"2026-09-24T09:19:06\"",
                    context.getBean(ObjectMapper.class)
                            .writeValueAsString(LocalDateTime.of(2026, 9, 24, 9, 19, 6)));
        }
    }
}
