package com.sysadminanywhere.model.monitoring;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class PasswordExpirationRule implements Rule {

    Map<String, String> parameters;

    public PasswordExpirationRule() {
    }

    @Override
    public String getName() {
        return "Password expiration notifier";
    }

    @Override
    public String getType() {
        return "PasswordExpirationRule";
    }

    @Override
    public String getDescription() {
        return "Password expiration notifier";
    }

    @Override
    public void execute(Map<String, String> parameters) {
        this.parameters = parameters;
        log.info("Executing PasswordExpirationRule at {}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
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