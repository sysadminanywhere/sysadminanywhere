package com.sysadminanywhere.client.directory;

import com.sysadminanywhere.common.directory.dto.ApiTokenCreateRequest;
import com.sysadminanywhere.common.directory.dto.ApiTokenCreatedResponse;
import com.sysadminanywhere.common.directory.dto.ApiTokenSummary;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;

public interface ApiTokensServiceClient {
    @GetExchange("/api/api-tokens")
    List<ApiTokenSummary> list();

    @PostExchange("/api/api-tokens")
    ApiTokenCreatedResponse create(@RequestBody ApiTokenCreateRequest request);

    @DeleteExchange("/api/api-tokens/{id}")
    void revoke(@PathVariable String id);
}
