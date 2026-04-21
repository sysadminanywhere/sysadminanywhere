package com.sysadminanywhere.inventory.config;

import com.sysadminanywhere.inventory.client.AuthServiceClient;
import com.sysadminanywhere.inventory.client.ComputersServiceClient;
import com.sysadminanywhere.inventory.client.LdapServiceClient;
import com.sysadminanywhere.inventory.client.WmiServiceClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class RestClientConfig {

    @Value("${app.services.directory.uri}")
    private String directoryServiceUri;

    @Bean
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }

    @Bean
    public RestClient directoryServiceRestClient(RestClient.Builder builder) {
        return builder
                .baseUrl(directoryServiceUri)
                .defaultHeaders(headers -> headers.add("Content-Type", "application/json"))
                .build();
    }

    @Bean
    public AuthServiceClient authServiceClient(RestClient directoryServiceRestClient) {
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builder()
                .exchangeAdapter(RestClientAdapter.create(directoryServiceRestClient))
                .build();
        return factory.createClient(AuthServiceClient.class);
    }

    @Bean
    public ComputersServiceClient computersServiceClient(RestClient directoryServiceRestClient) {
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builder()
                .exchangeAdapter(RestClientAdapter.create(directoryServiceRestClient))
                .build();
        return factory.createClient(ComputersServiceClient.class);
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
}
