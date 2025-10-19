package com.sysadminanywhere.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignClientConfig {

    @Bean
    public KeycloakFeignClientInterceptor keycloakFeignClientInterceptor() {
        return new KeycloakFeignClientInterceptor();
    }

}