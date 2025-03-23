package com.sysadminanywhere.service;

import com.sysadminanywhere.entity.RuleEntity;
import com.sysadminanywhere.model.monitoring.Rule;
import com.sysadminanywhere.repository.RuleRepository;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;

@Service
public class MonitoringService {

    private final RuleService ruleService;
    private final ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
    private final Map<Long, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    public MonitoringService(RuleService ruleService) {
        this.ruleService = ruleService;

        scheduler.initialize();
        List<RuleEntity> rules = ruleService.getAllRules();
        for (RuleEntity rule : rules) {
            scheduleRule(rule);
        }
    }

    private void scheduleRule(RuleEntity ruleEntity) {
        if (ruleEntity.getCronExpression() != null) {
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

    private void executeRule(RuleEntity ruleEntity) {
        Rule rule = ruleService.getRules(ruleEntity.getType());
        Map<String, Object> parameters = new HashMap<>(ruleEntity.getParameters());
        parameters.put("ruleName", ruleEntity.getName());
        parameters.put("executionTime", LocalDateTime.now());
        rule.execute(parameters);
    }

    public Page<RuleEntity> getAllRules(Pageable pageable, Map<String, String> filters) {
        return ruleService.getAllRules(pageable);
    }

    public void addRule(RuleEntity ruleEntity) {
        RuleEntity newRule = ruleService.addRule(ruleEntity);
        scheduleRule(newRule);
    }

    public void deleteRule(Long ruleId) {
        removeScheduledRule(ruleId);
        ruleService.deleteRule(ruleId);
    }

}