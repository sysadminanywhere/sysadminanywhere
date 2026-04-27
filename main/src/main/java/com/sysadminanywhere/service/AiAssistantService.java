package com.sysadminanywhere.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.sysadminanywhere.common.ai.model.AiNaturalSearchRequest;
import com.sysadminanywhere.common.ai.model.AiNaturalSearchResponse;
import com.sysadminanywhere.common.ai.model.AiSearchTranslation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
@RequiredArgsConstructor
public class AiAssistantService {

    private final OpenRouterLlmService openRouterLlmService;

    @Value("${app.ai.enabled:false}")
    private boolean aiEnabled;

    public AiNaturalSearchResponse translateNaturalSearch(AiNaturalSearchRequest request) {
        if (!aiEnabled) {
            return fallbackTranslation(request);
        }

        JsonNode llmJson = openRouterLlmService.completeJson(
                "You are an Active Directory and IT infrastructure expert. Translate natural language queries into LDAP filters and inventory search parameters.",
                buildPrompt(request),
                """
                {
                  "type": "object",
                  "required": ["ldapFilter", "inventoryFilters", "keywords", "explanation"],
                  "additionalProperties": false,
                  "properties": {
                    "ldapFilter": {
                      "type": "string",
                      "minLength": 1
                    },
                    "inventoryFilters": {
                      "type": "object",
                      "additionalProperties": { "type": "string" }
                    },
                    "keywords": {
                      "type": "array",
                      "items": { "type": "string", "minLength": 1 }
                    },
                    "explanation": {
                      "type": "string",
                      "minLength": 1
                    }
                  }
                }
                """
        );

        if (llmJson != null && llmJson.isObject()) {
            String ldapFilter = readText(llmJson, "ldapFilter", "(objectClass=*)");
            Map<String, String> inventoryFilters = readMap(llmJson, "inventoryFilters", Map.of());
            List<String> keywords = readStringList(llmJson, "keywords", List.of());
            String explanation = readText(llmJson, "explanation", "AI-generated search translation");

            AiSearchTranslation translation = AiSearchTranslation.builder()
                    .ldapFilter(ldapFilter)
                    .inventoryFilters(inventoryFilters)
                    .keywords(keywords)
                    .explanation(explanation)
                    .build();

            return AiNaturalSearchResponse.builder()
                    .translation(translation)
                    .success(true)
                    .build();
        }

        return fallbackTranslation(request);
    }

    private AiNaturalSearchResponse fallbackTranslation(AiNaturalSearchRequest request) {
        AiSearchTranslation translation = parseQueryWithRegex(request.getQuery(), request.getObjectType());

        return AiNaturalSearchResponse.builder()
                .translation(translation)
                .success(true)
                .errorMessage(aiEnabled ? "AI translation failed, using fallback" : "AI is disabled, using rule-based translation")
                .build();
    }

    private AiSearchTranslation parseQueryWithRegex(String query, String objectType) {
        String defaultObjectClass = objectType != null ? objectType : "user";
        String ldapFilter = "(objectClass=" + defaultObjectClass + ")";

        List<String> keywords = new ArrayList<>();
        Map<String, String> inventoryFilters = Map.of();

        String lowerQuery = query.toLowerCase();

        // Detect object type from query
        if (lowerQuery.contains("computer") || lowerQuery.contains("компьютер") || lowerQuery.contains("сервер")) {
            ldapFilter = "(objectClass=computer)";
            defaultObjectClass = "computer";
        } else if (lowerQuery.contains("group") || lowerQuery.contains("групп")) {
            ldapFilter = "(objectClass=group)";
            defaultObjectClass = "group";
        }

        // Extract percentage values for disk space
        Matcher percentMatcher = Pattern.compile("(\\d{1,2})\\s*%").matcher(query);
        if (percentMatcher.find()) {
            int percent = Integer.parseInt(percentMatcher.group(1));
            inventoryFilters = Map.of("diskFreePercentLt", String.valueOf(percent));
            keywords.add("disk:" + percent + "%");
        }

        // Extract days values for inactivity
        Matcher daysMatcher = Pattern.compile("(\\d{1,3})\\s*(day|день|дня|дней)").matcher(query);
        if (daysMatcher.find()) {
            int days = Integer.parseInt(daysMatcher.group(1));
            if (defaultObjectClass.equals("user")) {
                ldapFilter = "(&(objectClass=user)(lastLogonTimestamp<=" + (System.currentTimeMillis() - days * 86400000L) + "))";
                keywords.add("inactive:" + days + "days");
            }
        }

        // Extract OS version
        if (lowerQuery.contains("windows 10") || lowerQuery.contains("win10")) {
            inventoryFilters = Map.of("osVersion", "Windows 10");
            keywords.add("os:windows10");
        } else if (lowerQuery.contains("windows server") || lowerQuery.contains("server")) {
            inventoryFilters = Map.of("osType", "Server");
            keywords.add("os:server");
        }

        // Extract department/OU
        Matcher deptMatcher = Pattern.compile("(?:department|отдел|отделение)\\s*[:\\s]*([\\w\\s]+)", Pattern.CASE_INSENSITIVE).matcher(query);
        if (deptMatcher.find()) {
            String dept = deptMatcher.group(1).trim();
            ldapFilter = "(&(objectClass=" + defaultObjectClass + ")(department=" + dept + "*))";
            keywords.add("department:" + dept);
        }

        String explanation = "Rule-based translation: " + ldapFilter;
        if (!inventoryFilters.isEmpty()) {
            explanation += " with inventory filters: " + inventoryFilters;
        }

        return AiSearchTranslation.builder()
                .ldapFilter(ldapFilter)
                .inventoryFilters(inventoryFilters)
                .keywords(keywords)
                .explanation(explanation)
                .build();
    }

    private String buildPrompt(AiNaturalSearchRequest request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Query: ").append(request.getQuery()).append("\n");
        
        if (request.getObjectType() != null) {
            prompt.append("Object Type: ").append(request.getObjectType()).append("\n");
        }
        
        if (request.getLocale() != null) {
            prompt.append("Locale: ").append(request.getLocale()).append("\n");
        }
        
        prompt.append("\nGenerate appropriate LDAP filter and inventory search parameters.");
        
        return prompt.toString();
    }

    private String readText(JsonNode root, String fieldName, String fallback) {
        if (root == null || !root.isObject()) {
            return fallback;
        }
        JsonNode node = root.get(fieldName);
        if (node == null || node.isNull()) {
            return fallback;
        }
        String value = node.asText();
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private List<String> readStringList(JsonNode root, String fieldName, List<String> fallback) {
        if (root == null || !root.isObject()) {
            return fallback;
        }
        JsonNode node = root.get(fieldName);
        if (node == null || !node.isArray()) {
            return fallback;
        }
        List<String> parsed = java.util.stream.StreamSupport.stream(node.spliterator(), false)
                .map(JsonNode::asText)
                .filter(item -> item != null && !item.isBlank())
                .map(String::trim)
                .toList();
        return parsed.isEmpty() ? fallback : parsed;
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> readMap(JsonNode root, String fieldName, Map<String, String> fallback) {
        if (root == null || !root.isObject()) {
            return fallback;
        }
        JsonNode node = root.get(fieldName);
        if (node == null || !node.isObject()) {
            return fallback;
        }
        try {
            Map<String, String> result = new java.util.LinkedHashMap<>();
            node.fields().forEachRemaining(entry -> {
                String value = entry.getValue().asText();
                if (value != null && !value.isBlank()) {
                    result.put(entry.getKey(), value.trim());
                }
            });
            return result.isEmpty() ? fallback : result;
        } catch (Exception ex) {
            return fallback;
        }
    }
}
