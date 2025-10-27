package com.sysadminanywhere.inventory.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;

@RequiredArgsConstructor
@Slf4j
public class KeycloakFeignClientInterceptor implements RequestInterceptor {

    private final OAuth2AuthorizedClientService authorizedClientService;

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_TOKEN_TYPE = "Bearer";

    @Override
    public void apply(RequestTemplate template) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof OAuth2AuthenticationToken oauthToken) {
            String registrationId = oauthToken.getAuthorizedClientRegistrationId();
            OAuth2AuthorizedClient client = authorizedClientService
                    .loadAuthorizedClient(registrationId, oauthToken.getName());

            if (client != null && client.getAccessToken() != null) {
                String accessToken = client.getAccessToken().getTokenValue();
                template.header(AUTHORIZATION_HEADER, BEARER_TOKEN_TYPE + " " + accessToken);
            }
        }

        log.info("Feign Request: {} {}", template.method(), template.url());
        log.debug("Headers: {}", template.headers());
    }

}