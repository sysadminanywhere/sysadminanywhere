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
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
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

    private final ExpressionParser spel = new SpelExpressionParser();

    public void processEvents() {

        // 1️⃣ Загружаем только новые события (не связанные с инцидентами)
        List<EventEntity> newEvents = eventRepository.findByIncidentIdIsNull();
        if (newEvents.isEmpty()) return;

        LocalDateTime now = LocalDateTime.now();

        // 2️⃣ Обработка обычных сигналов
        for (Signal signal : signalLoader.getAll()) {
            if (signal.isMeta()) continue;

            LocalDateTime windowStart =
                    now.minusMinutes(signal.getAggregationWindowMinutes());

            List<EventEntity> signalEvents =
                    eventRepository.findEventsForSignal(
                            signal.getEventIds(),
                            windowStart,
                            now
                    );

            if (signalEvents.isEmpty()) continue;

            signalEvents = applyFilters(signal, signalEvents);

            Map<String, List<EventEntity>> grouped =
                    groupBy(signal, signalEvents);

            for (var entry : grouped.entrySet()) {
                List<EventEntity> groupEvents = entry.getValue();

                if (!withinWindow(signal, groupEvents)) continue;
                if (!thresholdOk(signal, groupEvents)) continue;
                if (!correlationOk(signal, groupEvents)) continue;

                createOrUpdateIncident(signal, groupEvents, entry.getKey());
            }
        }

        // 3️⃣ Обработка meta сигналов
        processMetaSignals(now);
    }

    // -------------------------
    // Группировка по groupBy
    private Map<String, List<EventEntity>> groupBy(Signal signal, List<EventEntity> events) {
        return events.stream().collect(Collectors.groupingBy(e -> {
            Map<String, Object> extra = parseExtra(e);
            return signal.getGroupBy().stream()
                    .map(f -> String.valueOf(extra.get(f)))
                    .collect(Collectors.joining("|"));
        }));
    }

    // -------------------------
    // Threshold
    private boolean thresholdOk(Signal s, List<EventEntity> events) {
        return s.getThreshold() == null || events.size() >= s.getThreshold();
    }

    // -------------------------
    // Проверка времени окна
    private boolean withinWindow(Signal s, List<EventEntity> events) {
        events.sort(Comparator.comparing(EventEntity::getTimeCreated));
        long minutes = Duration.between(
                events.get(0).getTimeCreated(),
                events.get(events.size() - 1).getTimeCreated()
        ).toMinutes();
        return minutes <= s.getAggregationWindowMinutes();
    }

    // -------------------------
    // Корреляция и последовательность
    private boolean correlationOk(Signal s, List<EventEntity> events) {
        if (!s.getOrderedSequence().isEmpty()) return checkSequence(events, s.getOrderedSequence());
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

    // -------------------------
    // Создание или обновление инцидента
    private void createOrUpdateIncident(Signal signal,
                                        List<EventEntity> events,
                                        String groupKey) {

        String dedupKey = signal.getId() + "|" + groupKey;
        IncidentEntity incident = incidentRepository.findByDeduplicationKey(dedupKey);

        Map<String, Object> context = buildContext(signal, events);

        if (incident == null) {
            // Создаём новый инцидент
            incident = IncidentEntity.builder()
                    .signalId(signal.getId())
                    .name(signal.getName())
                    .severity(resolveSeverity(signal, context))
                    .createdAt(LocalDateTime.now())
                    .firstEventTime(events.get(0).getTimeCreated())
                    .lastEventTime(events.get(events.size() - 1).getTimeCreated())
                    .deduplicationKey(dedupKey)
                    .eventCount(events.size())
                    .recommendation(render(signal.getRecommendationTemplate(), context))
                    .context(toJson(context))
                    .status(IncidentStatus.OPEN)
                    .build();
            incidentRepository.save(incident);
        } else {
            // Обновляем существующий инцидент
            incident.setEventCount(incident.getEventCount() + events.size());
            incident.setLastEventTime(events.get(events.size() - 1).getTimeCreated());
            incident.setUpdatedAt(LocalDateTime.now());
            incidentRepository.save(incident);
        }

        // Связываем события с инцидентом
        IncidentEntity finalIncident = incident;
        events.forEach(e -> e.setIncidentId(finalIncident.getId()));
        eventRepository.saveAll(events);
    }

    // -------------------------
    // Построение контекста
    private Map<String, Object> buildContext(Signal signal, List<EventEntity> events) {
        Map<String, Object> ctx = new HashMap<>();
        ctx.putAll(parseExtra(events.get(0)));
        ctx.put("event_count", events.size());
        ctx.put("signal_id", signal.getId());
        return ctx;
    }

    private Map<String, Object> parseExtra(EventEntity e) {
        try {
            return mapper.readValue(e.getExtra(), new TypeReference<>() {});
        } catch (Exception ex) {
            return Map.of();
        }
    }

    private String render(String template, Map<String, Object> ctx) {
        String result = template;
        for (var entry : ctx.entrySet()) {
            result = result.replace("{{" + entry.getKey() + "}}", String.valueOf(entry.getValue()));
        }
        return result;
    }

    private String toJson(Object obj) {
        try { return mapper.writeValueAsString(obj); }
        catch (Exception e) { return "{}"; }
    }

    // -------------------------
    // Severity rules
    private Severity resolveSeverity(Signal signal, Map<String, Object> ctx) {
        if (signal.getSeverity() == null) return Severity.MEDIUM;
        if (signal.getSeverity().getRules() != null) {
            for (SeverityRule rule : signal.getSeverity().getRules()) {
                if (evaluate(rule.getCondition(), ctx)) return rule.getSeverity();
            }
        }
        return signal.getSeverity().getDefaultSeverity();
    }

    private boolean evaluate(String condition, Map<String, Object> ctx) {
        StandardEvaluationContext context = new StandardEvaluationContext();
        ctx.forEach(context::setVariable);
        Expression exp = spel.parseExpression(condition);
        return Boolean.TRUE.equals(exp.getValue(context, Boolean.class));
    }

    // -------------------------
    // Meta сигналы (S21, S22)
    private void processMetaSignals(LocalDateTime now) {
        Signal repeated = signalLoader.get("S21");
        if (repeated != null) {
            List<IncidentEntity> recent = incidentRepository.findRecent(repeated.getAggregationWindowMinutes());
            Map<String, List<IncidentEntity>> grouped = recent.stream()
                    .collect(Collectors.groupingBy(i -> i.getSignalId() + "|" + i.getAffectedUser()));

            for (var entry : grouped.entrySet()) {
                if (entry.getValue().size() >= repeated.getThreshold()) {
                    IncidentEntity inc = IncidentEntity.builder()
                            .signalId("S21")
                            .name("Repeated Incident")
                            .severity(Severity.MEDIUM)
                            .meta(true)
                            .createdAt(now)
                            .deduplicationKey("S21|" + entry.getKey())
                            .eventCount(entry.getValue().size())
                            .build();
                    incidentRepository.save(inc);
                }
            }
        }
    }

    private List<EventEntity> applyFilters(Signal signal, List<EventEntity> events) {
        if (signal.getFilters() == null || signal.getFilters().isEmpty()) return events;

        return events.stream()
                .filter(e -> passesAllFilters(signal.getFilters(), e))
                .collect(Collectors.toList());
    }

    private boolean passesAllFilters(List<FilterRule> filters, EventEntity event) {
        Map<String, Object> extra = parseExtra(event);

        for (FilterRule filter : filters) {
            Object value = extra.get(filter.getField());
            if (!matches(value, filter.getOperator(), filter.getValue())) {
                return false;
            }
        }
        return true;
    }

    private boolean matches(Object actual, Operator operator, Object expected) {
        if (actual == null) return false;
        String act = String.valueOf(actual);
        String exp = String.valueOf(expected);

        return switch (operator) {
            case EQ -> act.equals(exp);
            case NE -> !act.equals(exp);
            case IN -> List.of(exp.split(",")).contains(act);
            case NOT_IN -> !List.of(exp.split(",")).contains(act);
            case GT -> compare(act, exp) > 0;
            case GTE -> compare(act, exp) >= 0;
            case LT -> compare(act, exp) < 0;
            case LTE -> compare(act, exp) <= 0;
            default -> false;
        };
    }

    private int compare(String a, String b) {
        try {
            double da = Double.parseDouble(a);
            double db = Double.parseDouble(b);
            return Double.compare(da, db);
        } catch (NumberFormatException ex) {
            return a.compareTo(b);
        }
    }

}