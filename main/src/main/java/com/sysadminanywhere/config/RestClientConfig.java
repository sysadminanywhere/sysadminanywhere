package com.sysadminanywhere.config;

import com.sysadminanywhere.client.N8nClient;
import com.sysadminanywhere.client.directory.*;
import com.sysadminanywhere.client.incident.IncidentServiceClient;
import com.sysadminanywhere.client.inventory.InventoryServiceClient;
import com.sysadminanywhere.client.servicedesk.TicketServiceClient;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class RestClientConfig {

    @Value("${app.services.directory.uri}")
    private String directoryServiceUri;

    @Value("${app.services.inventory.uri}")
    private String inventoryServiceUri;

    @Value("${app.services.incident.uri}")
    private String incidentServiceUri;

    @Value("${n8n.url:http://localhost:5678}")
    private String n8nUrl;

    @Value("${n8n.api-key}")
    private String n8nApiKey;

    @Bean
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }

    @Bean
    public ClientHttpRequestInterceptor jwtInterceptor() {
        return (request, body, execution) -> {
            VaadinSession session = VaadinSession.getCurrent();
            if (session != null) {
                String jwt = (String) session.getAttribute("jwt_token");
                if (jwt != null) {
                    request.getHeaders().add("Authorization", "Bearer " + jwt);
                }
            }
            return execution.execute(request, body);
        };
    }

    @Bean
    public ClientHttpRequestInterceptor n8nApiInterceptor() {
        return (request, body, execution) -> {
            request.getHeaders().add("X-N8N-API-KEY", n8nApiKey);
            request.getHeaders().add("Content-Type", "application/json");
            return execution.execute(request, body);
        };
    }

    @Bean
    public RestClient directoryServiceRestClient(RestClient.Builder builder) {
        return builder
                .baseUrl(directoryServiceUri)
                .defaultHeaders(headers -> headers.add("Content-Type", "application/json"))
                .requestInterceptor(jwtInterceptor())
                .requestInterceptor(CustomErrorDecoder.errorInterceptor())
                .build();
    }

    @Bean
    public RestClient inventoryServiceRestClient(RestClient.Builder builder) {
        return builder
                .baseUrl(inventoryServiceUri)
                .defaultHeaders(headers -> headers.add("Content-Type", "application/json"))
                .requestInterceptor(jwtInterceptor())
                .requestInterceptor(CustomErrorDecoder.errorInterceptor())
                .build();
    }

    @Bean
    public RestClient incidentServiceRestClient(RestClient.Builder builder) {
        return builder
                .baseUrl(incidentServiceUri)
                .defaultHeaders(headers -> headers.add("Content-Type", "application/json"))
                .requestInterceptor(jwtInterceptor())
                .requestInterceptor(CustomErrorDecoder.errorInterceptor())
                .build();
    }

    @Bean
    public RestClient n8nRestClient(RestClient.Builder builder) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(10000);

        return builder
                .baseUrl(n8nUrl)
                .requestFactory(factory)
                .requestInterceptor(n8nApiInterceptor())
                .build();
    }

    // Directory service clients
    @Bean
    public AuthServiceClient authServiceClient(RestClient directoryServiceRestClient) {
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builder()
                .exchangeAdapter(RestClientAdapter.create(directoryServiceRestClient))
                .build();
        return factory.createClient(AuthServiceClient.class);
    }

    @Bean
    public UsersServiceClient usersServiceClient(RestClient directoryServiceRestClient) {
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builder()
                .exchangeAdapter(RestClientAdapter.create(directoryServiceRestClient))
                .build();
        return factory.createClient(UsersServiceClient.class);
    }

    @Bean
    public GroupsServiceClient groupsServiceClient(RestClient directoryServiceRestClient) {
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builder()
                .exchangeAdapter(RestClientAdapter.create(directoryServiceRestClient))
                .build();
        return factory.createClient(GroupsServiceClient.class);
    }

    @Bean
    public ComputersServiceClient computersServiceClient(RestClient directoryServiceRestClient) {
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builder()
                .exchangeAdapter(RestClientAdapter.create(directoryServiceRestClient))
                .build();
        return factory.createClient(ComputersServiceClient.class);
    }

    @Bean
    public ContactsServiceClient contactsServiceClient(RestClient directoryServiceRestClient) {
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builder()
                .exchangeAdapter(RestClientAdapter.create(directoryServiceRestClient))
                .build();
        return factory.createClient(ContactsServiceClient.class);
    }

    @Bean
    public PrintersServiceClient printersServiceClient(RestClient directoryServiceRestClient) {
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builder()
                .exchangeAdapter(RestClientAdapter.create(directoryServiceRestClient))
                .build();
        return factory.createClient(PrintersServiceClient.class);
    }

    @Bean
    public LdapServiceClient ldapServiceClient(RestClient directoryServiceRestClient) {
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builder()
                .exchangeAdapter(RestClientAdapter.create(directoryServiceRestClient))
                .build();
        return factory.createClient(LdapServiceClient.class);
    }

    @Bean
    public WmiServiceClient wmiServiceClient(RestClient directoryServiceRestClient) {
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builder()
                .exchangeAdapter(RestClientAdapter.create(directoryServiceRestClient))
                .build();
        return factory.createClient(WmiServiceClient.class);
    }

    // Inventory service client
    @Bean
    public InventoryServiceClient inventoryServiceClient(RestClient inventoryServiceRestClient) {
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builder()
                .exchangeAdapter(RestClientAdapter.create(inventoryServiceRestClient))
                .build();
        return factory.createClient(InventoryServiceClient.class);
    }

    // Incident service client
    @Bean
    public IncidentServiceClient incidentServiceClient(RestClient incidentServiceRestClient) {
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builder()
                .exchangeAdapter(RestClientAdapter.create(incidentServiceRestClient))
                .build();
        return factory.createClient(IncidentServiceClient.class);
    }

    // Ticket service client (uses incident service)
    @Bean
    public TicketServiceClient ticketServiceClient(RestClient incidentServiceRestClient) {
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builder()
                .exchangeAdapter(RestClientAdapter.create(incidentServiceRestClient))
                .build();
        return factory.createClient(TicketServiceClient.class);
    }

    // N8n client
    @Bean
    public N8nClient n8nClient(RestClient n8nRestClient) {
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builder()
                .exchangeAdapter(RestClientAdapter.create(n8nRestClient))
                .build();
        return factory.createClient(N8nClient.class);
    }
}
