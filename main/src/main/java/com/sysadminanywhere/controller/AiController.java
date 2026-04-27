package com.sysadminanywhere.controller;

import com.sysadminanywhere.common.ai.model.AiNaturalSearchRequest;
import com.sysadminanywhere.common.ai.model.AiNaturalSearchResponse;
import com.sysadminanywhere.service.AiAssistantService;
import jakarta.annotation.security.RolesAllowed;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiAssistantService aiAssistantService;

    @PostMapping("/search/translate")
    @RolesAllowed("ADMIN")
    public AiNaturalSearchResponse translateSearch(@RequestBody AiNaturalSearchRequest request) {
        return aiAssistantService.translateNaturalSearch(request);
    }
}
