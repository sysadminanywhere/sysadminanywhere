package com.sysadminanywhere.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sysadminanywhere.entity.RuleEntity;
import com.sysadminanywhere.model.monitoring.Rule;
import com.sysadminanywhere.repository.RuleRepository;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;

@Service
@Slf4j
public class MonitoringService {

    private final RuleService ruleService;
    private final LogsService logsService;
    private final ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
    private final Map<Long, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    private final List<Rule> ruleImplementations;

    @Autowired
    private ApplicationContext context;

    @Autowired
    public MonitoringService(RuleService ruleService, LogsService logsService, List<Rule> ruleImplementations) {
        this.ruleService = ruleService;
        this.logsService = logsService;
        this.ruleImplementations = ruleImplementations;

        scheduler.initialize();
        List<RuleEntity> rules = ruleService.getAllRules();
        for (RuleEntity rule : rules) {
            scheduleRule(rule);
        }
    }

    private void scheduleRule(RuleEntity ruleEntity) {
        if (ruleEntity.getCronExpression() != null && ruleEntity.isActive()) {
            ScheduledFuture<?> future = scheduler.schedule(() -> executeRule(ruleEntity), new CronTrigger(ruleEntity.getCronExpression()));
            scheduledTasks.put(ruleEntity.getId(), future);
        }
    }

    private void removeScheduledRule(Long ruleId) {
        ScheduledFuture<?> future = scheduledTasks.remove(ruleId);
        if (future != null) {
            future.cancel(false);
        }
    }

    @SneakyThrows
    private void executeRule(RuleEntity ruleEntity) {
        ObjectMapper objectMapper = new ObjectMapper();
        Rule rule = createRuleInstance(ruleEntity.getType());
        Map<String, String> parameters = objectMapper.readValue(ruleEntity.getParameters(), new TypeReference<Map<String, String>>() {
        });

        String result = rule.execute(parameters);
        if (!result.isEmpty()) {
            logsService.addToLog(ruleEntity.getId(), result);
        }

        log.info("Executed rule: {} at {}", ruleEntity.getName(), LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }

    public Page<RuleEntity> getAllRules(Pageable pageable, Map<String, String> filters) {
        return ruleService.getAllRules(pageable);
    }

    public void addRule(RuleEntity ruleEntity) {
        RuleEntity newRule = ruleService.addRule(ruleEntity);
        scheduleRule(newRule);
    }

    public void updateRule(RuleEntity ruleEntity) {
        removeScheduledRule(ruleEntity.getId());
        RuleEntity newRule = ruleService.updateRule(ruleEntity.getId(), ruleEntity);
        scheduleRule(newRule);
    }

    public void deleteRule(Long ruleId) {
        removeScheduledRule(ruleId);
        ruleService.deleteRule(ruleId);
    }

    public List<Rule> getRuleImplementations() {
        return this.ruleImplementations;
    }

    public Rule createRuleInstance(String type) {
        Rule rule = ruleImplementations.stream().filter(c -> c.getType().equalsIgnoreCase(type)).findFirst().orElseThrow();
        return context.getBean(rule.getClass());
    }

    public Optional<RuleEntity> getRule(Long id) {
        return ruleService.getRule(id);
    }
}