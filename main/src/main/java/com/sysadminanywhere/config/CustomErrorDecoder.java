package com.sysadminanywhere.config;

import com.vaadin.flow.server.VaadinSession;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.core.context.SecurityContextHolder;


public class CustomErrorDecoder {

    public static ClientHttpRequestInterceptor errorInterceptor() {
        return (request, body, execution) -> {
            ClientHttpResponse response = execution.execute(request, body);
            if (response.getStatusCode().value() == 401) {
                // Handle 401 Unauthorized - logout user
                VaadinSession session = VaadinSession.getCurrent();
                if (session != null) {
                    session.setAttribute("jwt_token", null);
                    session.close();
                }
                SecurityContextHolder.clearContext();
            }
            return response;
        };
    }
}
