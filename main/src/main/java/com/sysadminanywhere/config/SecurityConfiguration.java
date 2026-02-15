package com.sysadminanywhere.config;

import com.sysadminanywhere.security.LogoutOnAccessDeniedHandler;
import com.sysadminanywhere.views.login.LoginView;
import com.vaadin.flow.spring.security.VaadinAwareSecurityContextHolderStrategyConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static com.vaadin.flow.spring.security.VaadinSecurityConfigurer.vaadin;

@EnableWebSecurity
@Configuration
@Import(VaadinAwareSecurityContextHolderStrategyConfiguration.class)
public class SecurityConfiguration {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain vaadinSecurityFilterChain(HttpSecurity http, LogoutOnAccessDeniedHandler logoutOnAccessDeniedHandler) throws Exception {

        http.authorizeHttpRequests(authorize -> authorize.requestMatchers("/images/*.png", "/*.css", "/icons/*.svg").permitAll());

        http.authorizeHttpRequests(authorize -> authorize.requestMatchers("/line-awesome/**").permitAll());

        http.exceptionHandling(ex->ex.accessDeniedHandler(logoutOnAccessDeniedHandler));

        http.with(vaadin(), vaadin -> {
            vaadin.loginView(LoginView.class);
        });

        return http.build();
    }

}