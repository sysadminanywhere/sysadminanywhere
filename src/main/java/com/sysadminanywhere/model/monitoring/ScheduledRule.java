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
        parameters = new HashMap<>();
        parameters.put("Test 1", "");
        parameters.put("Test 2", "");
    }

    @Override
    public String getName() {
        return "ScheduledRule";
    }

    @Override
    public Map<String, String> getParameters() {
        return parameters;
    }

    @Override
    public void execute(Map<String, String> parameters) {
        this.parameters = parameters;
        log.info("Executing ScheduledRule at {}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }

}