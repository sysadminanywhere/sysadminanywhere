package com.sysadminanywhere.incident.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class FeignConfiguration {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            // Извлечение текущего токена из контекста запроса (Token Relay)
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                String authorizationHeader = attributes.getRequest().getHeader("Authorization");
                if (authorizationHeader != null) {
                    // Проброс заголовка в исходящий запрос Feign
                    requestTemplate.header("Authorization", authorizationHeader);
                }
            }
        };

    }

}