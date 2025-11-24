package com.sysadminanywhere.config;

import feign.Logger;
import feign.codec.ErrorDecoder;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;

@Configuration
@RequiredArgsConstructor
public class FeignClientConfig {

    private final OAuth2AuthorizedClientService authorizedClientService;

    @Bean
    public KeycloakFeignClientInterceptor keycloakFeignClientInterceptor() {
        return new KeycloakFeignClientInterceptor(authorizedClientService);
    }

}