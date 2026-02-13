package com.sysadminanywhere.config;

import com.vaadin.flow.server.VaadinSession;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfiguration implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        VaadinSession session = VaadinSession.getCurrent();
        if (session != null) {
            String jwt = (String) session.getAttribute("jwt_token");
            if (jwt != null) {
                template.header("Authorization", "Bearer " + jwt);
            }
        }
    }

}