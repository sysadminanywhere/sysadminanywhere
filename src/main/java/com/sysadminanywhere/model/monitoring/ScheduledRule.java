package com.sysadminanywhere.model.monitoring;

import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@NoArgsConstructor
public class ScheduledRule implements Rule {

    Map<String, Object> parameters;

    @Override
    public void execute(Map<String, Object> parameters) {
        this.parameters = parameters;
        System.out.println("Executing ScheduledRule at " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }

}