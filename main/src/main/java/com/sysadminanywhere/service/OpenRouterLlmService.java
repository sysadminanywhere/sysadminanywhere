package com.sysadminanywhere.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Slf4j
public class OpenRouterLlmService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.ai.base-url:https://openrouter.ai/api/v1}")
    private String baseUrl;

    @Value("${app.ai.api-key:}")
    private String apiKey;

    @Value("${app.ai.model:google/gemma-3-27b-it:free}")
    private String model;

    @Value("${app.ai.app-url:https://sysadminanywhere.local}")
    private String appUrl;

    @Value("${app.ai.app-name:SysadminAnywhere}")
    private String appName;

    public String complete(String systemPrompt, String userPrompt) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("AI API key is not configured");
            return null;
        }

        RestClient client = RestClient.builder().baseUrl(baseUrl).build();

        Map<String, Object> payload = Map.of(
                "model", model,
                "temperature", 0.2,
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userPrompt)
                )
        );

        try {
            String response = client.post()
                    .uri("/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("HTTP-Referer", appUrl)
                    .header("X-Title", appName)
                    .body(payload)
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(response);
            JsonNode contentNode = root.path("choices").path(0).path("message").path("content");
            String content = contentNode.asText();
            return content == null || content.isBlank() ? null : content.trim();
        } catch (Exception ex) {
            log.warn("LLM API call failed. correlationId={} error={}", correlationId(), ex.getMessage());
            return null;
        }
    }

    public JsonNode completeJson(String systemPrompt, String userPrompt, String jsonSchemaHint) {
        String strictSystemPrompt = systemPrompt
                + "\nReturn only valid JSON without markdown fences or extra text."
                + "\nFollow this JSON schema exactly:\n" + jsonSchemaHint;

        String content = complete(strictSystemPrompt, userPrompt);
        if (content == null || content.isBlank()) {
            return null;
        }

        JsonNode schemaNode = parseSchema(jsonSchemaHint);
        if (schemaNode == null) {
            log.warn("LLM schema parsing failed. correlationId={} Invalid JSON schema hint.", correlationId());
            return null;
        }

        try {
            JsonNode candidate = objectMapper.readTree(content);
            return validateCandidate(candidate, schemaNode, "raw");
        } catch (Exception firstParseException) {
            String extracted = extractJsonObject(content);
            if (extracted == null) {
                log.warn("LLM response rejected. correlationId={} Cannot extract JSON object from content.", correlationId());
                return null;
            }
            try {
                JsonNode candidate = objectMapper.readTree(extracted);
                return validateCandidate(candidate, schemaNode, "extracted");
            } catch (Exception secondParseException) {
                log.warn("LLM response rejected. correlationId={} JSON parsing failed after extraction: {}",
                        correlationId(), secondParseException.getMessage());
                return null;
            }
        }
    }

    private String extractJsonObject(String rawContent) {
        int start = rawContent.indexOf('{');
        int end = rawContent.lastIndexOf('}');
        if (start < 0 || end < 0 || end <= start) {
            return null;
        }
        return rawContent.substring(start, end + 1);
    }

    private JsonNode parseSchema(String schemaText) {
        try {
            return objectMapper.readTree(schemaText);
        } catch (Exception ex) {
            return null;
        }
    }

    private JsonNode validateCandidate(JsonNode candidate, JsonNode schemaNode, String sourceType) {
        try {
            JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
            JsonSchema schema = factory.getSchema(schemaNode);
            Set<ValidationMessage> violations = schema.validate(candidate);
            if (violations.isEmpty()) {
                return candidate;
            }

            String details = violations.stream()
                    .map(ValidationMessage::getMessage)
                    .limit(10)
                    .reduce((left, right) -> left + " | " + right)
                    .orElse("unknown schema violation");
            log.warn("LLM response rejected by JSON schema. correlationId={} source={} details={}",
                    correlationId(), sourceType, details);
            return null;
        } catch (Exception ex) {
            log.warn("LLM response validation error. correlationId={} source={} error={}",
                    correlationId(), sourceType, ex.getMessage());
            return null;
        }
    }

    private String correlationId() {
        String correlationId = MDC.get("correlationId");
        return correlationId == null || correlationId.isBlank() ? "n/a" : correlationId;
    }
}
