package com.sysadminanywhere.model.monitoring;

import com.vaadin.flow.component.textfield.TextField;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class PingRule implements Rule {

    TextField txtAddress = new TextField("Address");

    public PingRule() {
    }

    @Override
    public String getName() {
        return "Ping";
    }

    @Override
    public String getType() {
        return "PingRule";
    }

    @Override
    public String getDescription() {
        return "Ping";
    }

    @Override
    public String execute(Map<String, String> parameters) {
        log.info("Executing PingRule at {}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        if (!parameters.isEmpty() && parameters.containsKey("address")) {
            try {
                String host = parameters.get("address");
                InetAddress address = InetAddress.getByName(host);
                boolean reachable = address.isReachable(1000);

                if (!reachable)
                    return "Host '" + host + "' is not reachable";

            } catch (Exception e) {
                return "Error: " + e.getMessage();
            }
        }

        return "";
    }

    @Override
    public String getDefaultCron() {
        return "0 * * * * *";
    }

    @Override
    public List<com.vaadin.flow.component.Component> getControls(Map<String, String> parameters) {
        List<com.vaadin.flow.component.Component> components = new ArrayList<>();

        if (!parameters.isEmpty()) {
            if (parameters.containsKey("address")) txtAddress.setValue(parameters.get("address"));
        }

        components.add(txtAddress);

        return components;
    }

    @Override
    public Map<String, String> getParameters() {
        Map<String, String> map = new HashMap<>();

        map.put("address", txtAddress.getValue());

        return map;
    }

}