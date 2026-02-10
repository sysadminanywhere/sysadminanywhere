package com.sysadminanywhere.incident.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sysadminanywhere.incident.entity.Event;
import com.sysadminanywhere.incident.entity.Incident;
import com.sysadminanywhere.incident.entity.IncidentEvent;
import com.sysadminanywhere.incident.repository.EventRepository;
import com.sysadminanywhere.incident.repository.IncidentEventRepository;
import com.sysadminanywhere.incident.repository.IncidentRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class IncidentService {

    private final EventRepository eventRepository;
    private final IncidentRepository incidentRepository;
    private final IncidentEventRepository incidentEventRepository;
    private final SignalConfigLoader configLoader;

    public IncidentService(EventRepository eventRepository,
                           IncidentRepository incidentRepository,
                           IncidentEventRepository incidentEventRepository,
                           SignalConfigLoader configLoader) {
        this.eventRepository = eventRepository;
        this.incidentRepository = incidentRepository;
        this.incidentEventRepository = incidentEventRepository;
        this.configLoader = configLoader;
    }

    public void processEvents() {
        List<Event> events = eventRepository.findByProcessedFalseOrderByTimestampAsc();

        for (Event e : events) {
            if (e.getSignalId() == null) continue;

            Map<String, Object> config = configLoader.getSignal(e.getSignalId());
            if (config == null) continue;

            // --- Вычисляем ключ группировки по groupBy ---
            @SuppressWarnings("unchecked")
            List<String> groupBy = (List<String>) config.getOrDefault("groupBy", List.of("userName", "sourceHost"));
            Map<String, String> groupKey = new HashMap<>();
            for (String field : groupBy) {
                switch (field) {
                    case "user_name" -> groupKey.put("user_name", e.getUserName());
                    case "source_host" -> groupKey.put("source_host", e.getSourceHost());
                    case "target_host" -> groupKey.put("target_host", e.getTargetHost());
                    case "object_name" -> groupKey.put("object_name", e.getExtra() != null ? extractJsonField(e.getExtra(), "object_name") : "");
                    case "target_group" -> groupKey.put("target_group", e.getExtra() != null ? extractJsonField(e.getExtra(), "target_group") : "");
                }
            }

            // --- aggregationWindowMinutes ---
            int window = (int) config.getOrDefault("aggregationWindowMinutes", 10);
            LocalDateTime windowStart = e.getTimestamp().minusMinutes(window);

            // --- ищем существующий инцидент по сигналу и groupKey в пределах окна ---
            List<Incident> existing = incidentRepository.findBySignalIdAndAffectedUserAndStatus(
                            e.getSignalId(), groupKey.getOrDefault("user_name", null), "new"
                    ).stream()
                    .filter(i -> !i.getLastUpdated().isBefore(windowStart))
                    .toList();

            Incident incident;
            if (existing.isEmpty()) {
                // --- Создаём новый инцидент ---
                incident = new Incident();
                incident.setSignalId(e.getSignalId());
                incident.setTitle(config.get("name") + ": " + groupKey.getOrDefault("user_name", "N/A"));
                incident.setAffectedUser(groupKey.getOrDefault("user_name", "N/A"));
                incident.setSourceHost(groupKey.getOrDefault("source_host", "N/A"));
                incident.setTimestamp(e.getTimestamp());
                incident.setLastUpdated(e.getTimestamp());
                incident.setSeverity(calculateSeverity(config, groupKey));
                incident.setRecommendation(generateRecommendation(config, groupKey));
                incident.setExplanation(generateExplanation(config, groupKey, e));
                incidentRepository.save(incident);
            } else {
                // --- Обновляем существующий инцидент ---
                incident = existing.get(0);
                incident.setLastUpdated(e.getTimestamp());
                incidentRepository.save(incident);
            }

            // --- Связываем событие с инцидентом ---
            IncidentEvent ie = new IncidentEvent();
            ie.setIncidentId(incident.getId());
            ie.setEventId(e.getId());
            incidentEventRepository.save(ie);

            // --- Отмечаем событие как обработанное ---
            e.setProcessed(true);
            eventRepository.save(e);
        }
    }

    // --- Вычисление severity с учетом правил ---
    private String calculateSeverity(Map<String, Object> config, Map<String, String> groupKey) {
        Map<String, Object> severityConfig = (Map<String, Object>) config.get("severity");
        String severity = (String) severityConfig.getOrDefault("default", "medium");

        @SuppressWarnings("unchecked")
        List<Map<String, String>> rules = (List<Map<String, String>>) severityConfig.getOrDefault("rules", List.of());
        for (Map<String, String> rule : rules) {
            String condition = rule.get("condition");
            if (evaluateCondition(condition, groupKey)) {
                severity = rule.get("severity");
            }
        }
        return severity;
    }

    // --- Простая проверка условий вида "user_name IN ('Domain Admins')" ---
    private boolean evaluateCondition(String condition, Map<String, String> groupKey) {
        if (condition.contains("IN")) {
            String[] parts = condition.split("IN");
            String field = parts[0].trim();
            String values = parts[1].trim().replace("(", "").replace(")", "").replace("'", "");
            List<String> list = Arrays.stream(values.split(",")).map(String::trim).toList();
            return list.contains(groupKey.get(field));
        }
        return false;
    }

    // --- Генерация текста рекомендации ---
    private String generateRecommendation(Map<String, Object> config, Map<String, String> groupKey) {
        String template = (String) config.get("recommendationTemplate");
        for (Map.Entry<String, String> entry : groupKey.entrySet()) {
            template = template.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        return "[\"" + template + "\"]"; // сохраняем как JSON массив
    }

    // --- Генерация объяснения (пока простое) ---
    private String generateExplanation(Map<String, Object> config, Map<String, String> groupKey, Event e) {
        return "Инцидент '" + config.get("name") + "' создан для пользователя " + groupKey.getOrDefault("user_name", "N/A") +
                " с событиями с хоста " + groupKey.getOrDefault("source_host", "N/A") +
                " на основе события " + e.getEventId();
    }

    // --- Вспомогательная функция для JSONB extra ---
    private String extractJsonField(String json, String field) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(json);
            return node.has(field) ? node.get(field).asText() : "";
        } catch (Exception ex) {
            return "";
        }
    }
}