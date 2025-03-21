package com.sysadminanywhere.service;

import com.sysadminanywhere.model.monitoring.Rule;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class MonitoringService {

    private final List<Rule> rules;
    private final ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();

    public MonitoringService(List<Rule> rules) {
        this.rules = rules;
        scheduler.initialize();

        for (Rule rule : rules) {
            if (rule.getCronExpression() != null) {
                scheduler.schedule(rule::execute, new CronTrigger(rule.getCronExpression()));
            }
        }
    }

}