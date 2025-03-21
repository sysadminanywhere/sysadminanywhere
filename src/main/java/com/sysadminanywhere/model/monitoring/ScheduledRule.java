package com.sysadminanywhere.model.monitoring;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class ScheduledRule implements Rule {

    Map<String, Object> parameters;

    @Override
    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    @Override
    public String getName() {
        return "ScheduledRule";
    }

    @Override
    public void execute() {
        System.out.println("Executing ScheduledRule at " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }

    @Override
    public String getCronExpression() {
        return "0 0/1 * * * *";
    }

}