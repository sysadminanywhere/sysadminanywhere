package com.sysadminanywhere.incident.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sysadminanywhere.incident.entity.EventEntity;
import com.sysadminanywhere.incident.entity.IncidentEntity;
import com.sysadminanywhere.incident.model.*;
import com.sysadminanywhere.incident.repository.EventRepository;
import com.sysadminanywhere.incident.repository.IncidentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class IncidentService {

    private final EventRepository eventRepository;
    private final IncidentRepository incidentRepository;
    private final SignalLoader signalLoader;
    private final ObjectMapper mapper;

    public void processEvents() {

        // 🔥 Берем ВСЕ необработанные события
        List<EventEntity> unprocessed = eventRepository.findByIncidentIdIsNull();
        if (unprocessed.isEmpty()) return;

        // сортируем один раз
        unprocessed.sort(Comparator.comparing(EventEntity::getTimeCreated));

        for (Signal signal : signalLoader.getAll()) {

            if (signal.isMeta()) continue;

            // фильтруем только события этого сигнала
            List<EventEntity> signalEvents = unprocessed.stream()
                    .filter(e -> signal.getEventIds().contains(e.getEventId()))
                    .collect(Collectors.toList());

            if (signalEvents.isEmpty()) continue;

            signalEvents = applyFilters(signal, signalEvents);

            // группировка
            Map<String, List<EventEntity>> grouped = groupBy(signal, signalEvents);

            for (var entry : grouped.entrySet()) {

                List<EventEntity> events = entry.getValue();

                // 🔥 теперь окно считается относительно первого события группы
                List<List<EventEntity>> windows =
                        splitByAggregationWindow(signal, events);

                for (List<EventEntity> windowEvents : windows) {

                    if (!thresholdOk(signal, windowEvents)) continue;
                    if (!correlationOk(signal, windowEvents)) continue;

                    createIncident(signal, windowEvents, entry.getKey());
                }
            }
        }
    }

    // =========================
    // Разбиваем события на окна
    // =========================
    private List<List<EventEntity>> splitByAggregationWindow(
            Signal signal,
            List<EventEntity> events) {

        List<List<EventEntity>> result = new ArrayList<>();

        events.sort(Comparator.comparing(EventEntity::getTimeCreated));

        List<EventEntity> current = new ArrayList<>();
        EventEntity first = null;

        for (EventEntity e : events) {

            if (first == null) {
                first = e;
                current.add(e);
                continue;
            }

            long minutes = Duration.between(
                    first.getTimeCreated(),
                    e.getTimeCreated()
            ).toMinutes();

            if (minutes <= signal.getAggregationWindowMinutes()) {
                current.add(e);
            } else {
                result.add(current);
                current = new ArrayList<>();
                current.add(e);
                first = e;
            }
        }

        if (!current.isEmpty()) result.add(current);

        return result;
    }

    private boolean thresholdOk(Signal s, List<EventEntity> events) {
        return s.getThreshold() == null || events.size() >= s.getThreshold();
    }

    private boolean correlationOk(Signal s, List<EventEntity> events) {

        if (!s.getOrderedSequence().isEmpty()) {
            return checkSequence(events, s.getOrderedSequence());
        }

        if (!s.getCorrelatedEventIds().isEmpty()) {
            Set<Integer> present = events.stream()
                    .map(EventEntity::getEventId)
                    .collect(Collectors.toSet());
            return present.containsAll(s.getCorrelatedEventIds());
        }

        return true;
    }

    private boolean checkSequence(List<EventEntity> events, List<Integer> sequence) {

        events.sort(Comparator.comparing(EventEntity::getTimeCreated));

        int idx = 0;

        for (EventEntity e : events) {
            if (e.getEventId().equals(sequence.get(idx))) {
                idx++;
                if (idx == sequence.size()) return true;
            }
        }

        return false;
    }

    private Map<String, List<EventEntity>> groupBy(
            Signal signal,
            List<EventEntity> events) {

        return events.stream().collect(Collectors.groupingBy(e -> {

            Map<String, Object> extra = parseExtra(e);

            return signal.getGroupBy().stream()
                    .map(f -> String.valueOf(extra.getOrDefault(f, "")))
                    .collect(Collectors.joining("|"));
        }));
    }

    private void createIncident(
            Signal signal,
            List<EventEntity> events,
            String groupKey) {

        events.sort(Comparator.comparing(EventEntity::getTimeCreated));

        IncidentEntity incident = IncidentEntity.builder()
                .signalId(signal.getId())
                .name(signal.getName())
                .severity(signal.getSeverity().getDefaultSeverity())
                .createdAt(LocalDateTime.now())
                .firstEventTime(events.get(0).getTimeCreated())
                .lastEventTime(events.get(events.size() - 1).getTimeCreated())
                .eventCount(events.size())
                .deduplicationKey(signal.getId() + "|" + groupKey + "|" +
                        events.get(0).getTimeCreated())
                .status(IncidentStatus.OPEN)
                .machineName(events.get(0).getMachineName())
                .recommendation(renderRecommendation(signal, events))
                .build();

        incidentRepository.save(incident);

        long incidentId = incident.getId();

        events.forEach(e -> e.setIncidentId(incidentId));
        eventRepository.saveAll(events);
    }

    private Map<String, Object> parseExtra(EventEntity e) {
        try {
            return mapper.readValue(
                    e.getExtra(),
                    new TypeReference<Map<String, Object>>() {}
            );
        } catch (Exception ex) {
            return Map.of();
        }
    }

    private List<EventEntity> applyFilters(
            Signal signal,
            List<EventEntity> events) {

        if (signal.getFilters() == null || signal.getFilters().isEmpty())
            return events;

        return events.stream()
                .filter(e -> passesAllFilters(signal.getFilters(), e))
                .collect(Collectors.toList());
    }

    private boolean passesAllFilters(
            List<FilterRule> filters,
            EventEntity event) {

        Map<String, Object> extra = parseExtra(event);

        for (FilterRule filter : filters) {
            Object value = extra.get(filter.getField());
            if (!Objects.equals(String.valueOf(value),
                    String.valueOf(filter.getValue())))
                return false;
        }

        return true;
    }

    private String renderRecommendation(
            Signal signal,
            List<EventEntity> events) {

        if (signal.getRecommendationTemplate() == null || signal.getRecommendationTemplate().isBlank()) {
            return "";
        }

        EventEntity first = events.get(0);
        EventEntity last = events.get(events.size() - 1);

        Map<String, Object> context = new HashMap<>();

        // 🔹 1. Все поля из EventData
        context.putAll(parseExtra(first));

        // 🔹 2. Системные поля события
        context.put("MachineName", first.getMachineName());
        context.put("EventId", first.getEventId());

        // 🔹 3. Групповые поля
        context.put("eventCount", events.size());
        context.put("firstEventTime", first.getTimeCreated());
        context.put("lastEventTime", last.getTimeCreated());

        // 🔹 4. Поля сигнала
        context.put("signalId", signal.getId());
        context.put("signalName", signal.getName());

        return replacePlaceholders(signal.getRecommendationTemplate(), context);
    }

    private String replacePlaceholders(String template, Map<String, Object> context) {

        String result = template;

        for (Map.Entry<String, Object> entry : context.entrySet()) {
            String key = "{{" + entry.getKey() + "}}";
            String value = entry.getValue() != null
                    ? entry.getValue().toString()
                    : "";

            result = result.replace(key, value);
        }

        // 🔥 Если остались незаполненные {{...}} — убираем
        result = result.replaceAll("\\{\\{.*?}}", "");

        return result;
    }

}