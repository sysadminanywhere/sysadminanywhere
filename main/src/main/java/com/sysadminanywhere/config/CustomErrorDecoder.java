package com.sysadminanywhere.config;

import com.vaadin.flow.server.VaadinSession;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.security.core.context.SecurityContextHolder;

public class CustomErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        if (response.status() == 401) {
            // Handle 401 Unauthorized - logout user
            VaadinSession session = VaadinSession.getCurrent();
            if (session != null) {
                session.setAttribute("jwt_token", null);
                session.close();
            }
            SecurityContextHolder.clearContext();
        }
        return new Default().decode(methodKey, response);
    }
}
