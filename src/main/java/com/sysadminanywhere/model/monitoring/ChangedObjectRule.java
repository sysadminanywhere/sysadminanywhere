package com.sysadminanywhere.model.monitoring;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Component
@Scope("prototype")
@Slf4j
public class ChangedObjectRule implements Rule {

    Map<String, String> parameters;

    public ChangedObjectRule() {
    }

    @Override
    public String getName() {
        return "Changed selected object";
    }

    @Override
    public String getType() {
        return "ChangedObjectRule";
    }

    @Override
    public String getDescription() {
        return "Changed selected object";
    }

    @Override
    public String execute(Map<String, String> parameters) {
        this.parameters = parameters;
        log.info("Executing ChangedObjectRule at {}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        return "";
    }

    @Override
    public String getDefaultCron() {
        return "0 * * * * *";
    }

    @Override
    public List<com.vaadin.flow.component.Component> getControls(Map<String, String> parameters) {
        return List.of();
    }

    @Override
    public Map<String, String> getParameters() {
        return parameters;
    }

}