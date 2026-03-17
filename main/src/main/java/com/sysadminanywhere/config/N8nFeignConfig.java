package com.sysadminanywhere.config;

import feign.Logger;
import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class N8nFeignConfig {

    @Value("${n8n.api-key}")
    private String apiKey;

    @Bean
    public RequestInterceptor apiKeyInterceptor() {
        return requestTemplate -> {
            requestTemplate.header("X-N8N-API-KEY", apiKey);
            requestTemplate.header("Content-Type", "application/json");
        };
    }

}