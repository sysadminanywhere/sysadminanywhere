package com.sysadminanywhere.config;

import com.sysadminanywhere.service.CustomOidcUserService;
import com.vaadin.flow.spring.security.VaadinAwareSecurityContextHolderStrategyConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.SecurityFilterChain;

import java.util.*;

@EnableWebSecurity
@Configuration
@Import(VaadinAwareSecurityContextHolderStrategyConfiguration.class)
public class SecurityConfig {

    @Value("${app.keycloak.logout-uri}")
    String keycloakLogoutUrl;

    private final CustomOidcUserService customOidcUserService;

    public SecurityConfig(CustomOidcUserService customOidcUserService) {
        this.customOidcUserService = customOidcUserService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.disable())

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/images/*.png", "/line-awesome/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )

                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/oauth2/authorization/keycloak")
                        .userInfoEndpoint(userInfo -> userInfo
                                .oidcUserService(customOidcUserService))
                )

                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessHandler((request, response, authentication) -> {
                            String idToken = null;
                            if (authentication != null && authentication.getPrincipal() instanceof OidcUser) {
                                OidcUser oidcUser = (OidcUser) authentication.getPrincipal();
                                idToken = oidcUser.getIdToken().getTokenValue();
                            }

                            //String keycloakLogoutUrl = "http://localhost:8880/realms/sysadminanywhere/protocol/openid-connect/logout";
                            if (idToken != null) {
                                keycloakLogoutUrl += "?id_token_hint=" + idToken + "&post_logout_redirect_uri=" + request.getRequestURL().toString().replace(request.getRequestURI(), "");
                            }

                            request.getSession().invalidate();
                            response.sendRedirect(keycloakLogoutUrl);
                        })
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                );

        return http.build();
    }

}