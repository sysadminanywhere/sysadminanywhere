package com.sysadminanywhere.incident.config;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class RestClientConfigTest {
    @Test
    void registersRestClientBuilder() {
        try (AnnotationConfigApplicationContext context =
                     new AnnotationConfigApplicationContext(RestClientConfig.class)) {
            assertNotNull(context.getBean(RestClient.Builder.class));
        }
    }
}
