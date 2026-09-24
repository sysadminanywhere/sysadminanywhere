package com.sysadminanywhere.config;

import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.component.UI;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.core.context.SecurityContextHolder;


public class CustomErrorDecoder {

    public static ClientHttpRequestInterceptor errorInterceptor() {
        return (request, body, execution) -> {
            ClientHttpResponse response = execution.execute(request, body);
            int status = response.getStatusCode().value();
            if (status == 401 || status == 403) {
                // A downstream service can return 403 when its JWT has expired.
                VaadinSession session = VaadinSession.getCurrent();
                if (session != null) {
                    session.setAttribute("jwt_token", null);
                    try {
                        session.getSession().invalidate();
                    } catch (IllegalStateException ignored) {
                        // The HTTP session may already have been invalidated.
                    }
                }
                SecurityContextHolder.clearContext();

                UI ui = UI.getCurrent();
                if (ui != null) {
                    ui.getPage().setLocation("/login");
                }
            }
            return response;
        };
    }
}
