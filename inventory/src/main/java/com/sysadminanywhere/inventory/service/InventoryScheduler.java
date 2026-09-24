package com.sysadminanywhere.inventory.service;

import com.sysadminanywhere.common.inventory.model.InventorySchedule;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.util.concurrent.ScheduledFuture;

@Component
public class InventoryScheduler {
    private final InventoryService inventoryService;
    private final ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
    private final String defaultCron;
    private ScheduledFuture<?> task;
    private String cron;
    private boolean enabled = true;

    public InventoryScheduler(InventoryService inventoryService,
                              @Value("${cron.expression:0 0 0 * * *}") String defaultCron) {
        this.inventoryService = inventoryService;
        this.defaultCron = defaultCron;
        this.cron = defaultCron;
    }

    @PostConstruct
    void start() {
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("inventory-scheduler-");
        scheduler.initialize();
        schedule();
    }

    public synchronized InventorySchedule getSchedule() {
        return new InventorySchedule(cron, enabled);
    }

    public synchronized InventorySchedule update(String newCron, boolean newEnabled) {
        String value = newCron == null || newCron.isBlank() ? defaultCron : newCron.trim();
        if (!CronExpression.isValidExpression(value)) {
            throw new IllegalArgumentException("Invalid cron expression");
        }
        cron = value;
        enabled = newEnabled;
        schedule();
        return getSchedule();
    }

    private void schedule() {
        if (task != null) task.cancel(false);
        task = enabled ? scheduler.schedule(inventoryService::scan,
                new CronTrigger(cron, ZoneId.systemDefault())) : null;
    }

    @PreDestroy
    void stop() {
        if (task != null) task.cancel(false);
        scheduler.shutdown();
    }
}
