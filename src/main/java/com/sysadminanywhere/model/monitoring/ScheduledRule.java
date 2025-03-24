package com.sysadminanywhere.model.monitoring;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class ScheduledRule implements Rule {

    Map<String, String> parameters;

    public ScheduledRule() {
    }

    @Override
    public String getName() {
        return "Scheduled rule";
    }

    @Override
    public String getType() {
        return "ScheduledRule";
    }

    @Override
    public String getDescription() {
        return "Rule for testing";
    }

    @Override
    public void execute(Map<String, String> parameters) {
        this.parameters = parameters;
        log.info("Executing ScheduledRule at {}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }

}