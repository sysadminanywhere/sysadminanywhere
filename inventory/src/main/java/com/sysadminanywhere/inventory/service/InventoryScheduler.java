package com.sysadminanywhere.inventory.service;

import com.sysadminanywhere.common.inventory.model.InventorySchedule;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.sql.init.dependency.DependsOnDatabaseInitialization;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.ScheduledFuture;

@Component
@DependsOnDatabaseInitialization
public class InventoryScheduler {
    private final InventoryService inventoryService;
    private final ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
    private final String defaultCron;
    private final JdbcTemplate jdbc;
    private final ZoneId zone = ZoneId.systemDefault();
    private ScheduledFuture<?> task;
    private String cron;
    private boolean enabled = true;

    public InventoryScheduler(InventoryService inventoryService, JdbcTemplate jdbc,
                              @Value("${cron.expression:0 0 0 * * *}") String defaultCron) {
        this.inventoryService = inventoryService;
        this.jdbc = jdbc;
        this.defaultCron = defaultCron;
        this.cron = defaultCron;
    }

    @PostConstruct
    void start() {
        jdbc.query("SELECT cron, enabled FROM inventory_schedule WHERE id = 1", row -> {
            cron = row.getString("cron");
            enabled = row.getBoolean("enabled");
        });
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("inventory-scheduler-");
        scheduler.initialize();
        schedule();
    }

    public synchronized InventorySchedule getSchedule() {
        ZonedDateTime next = enabled ? CronExpression.parse(cron).next(ZonedDateTime.now(zone)) : null;
        Instant nextRun = next == null ? null : next.toInstant();
        return new InventorySchedule(cron, enabled, nextRun, zone.getId());
    }

    public synchronized InventorySchedule update(String newCron, boolean newEnabled) {
        String value = newCron == null || newCron.isBlank() ? defaultCron : newCron.trim();
        if (!CronExpression.isValidExpression(value)) {
            throw new IllegalArgumentException("Invalid cron expression");
        }
        jdbc.update("""
                INSERT INTO inventory_schedule (id, cron, enabled) VALUES (1, ?, ?)
                ON CONFLICT (id) DO UPDATE SET cron = EXCLUDED.cron, enabled = EXCLUDED.enabled
                """, value, newEnabled);
        cron = value;
        enabled = newEnabled;
        schedule();
        return getSchedule();
    }

    private void schedule() {
        if (task != null) task.cancel(false);
        task = enabled ? scheduler.schedule(inventoryService::scan,
                new CronTrigger(cron, zone)) : null;
    }

    @PreDestroy
    void stop() {
        if (task != null) task.cancel(false);
        scheduler.shutdown();
    }
}
