package com.sysadminanywhere.model.monitoring;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class ScheduledRule implements Rule {

    Map<String, Object> parameters;

    @Override
    public void execute(Map<String, Object> parameters) {
        this.parameters = parameters;
        System.out.println("Executing ScheduledRule at " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }

}