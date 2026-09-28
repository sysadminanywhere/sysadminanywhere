package com.sysadminanywhere.config;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.mock.http.client.MockClientHttpResponse;

import java.net.URI;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RetryingHttpInterceptorTest {
    @Test
    void retriesGetAfterServerError() throws Exception {
        var interceptor = new RetryingHttpInterceptor(3, 0);
        var request = new MockClientHttpRequest(HttpMethod.GET, URI.create("http://localhost/test"));
        var calls = new AtomicInteger();
        ClientHttpRequestExecution execution = (ignoredRequest, ignoredBody) -> calls.incrementAndGet() == 1
                ? new MockClientHttpResponse(new byte[0], 503)
                : new MockClientHttpResponse(new byte[0], 200);

        ClientHttpResponse response = interceptor.intercept(request, new byte[0], execution);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(2, calls.get());
    }
}
