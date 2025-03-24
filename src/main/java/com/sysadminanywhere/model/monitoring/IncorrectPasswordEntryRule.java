package com.sysadminanywhere.model.monitoring;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Component
@Slf4j
public class IncorrectPasswordEntryRule implements Rule {

    Map<String, String> parameters;

    public IncorrectPasswordEntryRule() {
    }

    @Override
    public String getName() {
        return "Incorrect password entry";
    }

    @Override
    public String getType() {
        return "IncorrectPasswordEntryRule";
    }

    @Override
    public String getDescription() {
        return "Incorrect password entry";
    }

    @Override
    public void execute(Map<String, String> parameters) {
        this.parameters = parameters;
        log.info("Executing IncorrectPasswordEntryRule at {}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }

}