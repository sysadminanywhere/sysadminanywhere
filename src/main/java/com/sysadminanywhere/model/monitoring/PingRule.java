package com.sysadminanywhere.model.monitoring;

import com.sysadminanywhere.service.EmailService;
import com.vaadin.flow.component.textfield.TextField;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Scope("prototype")
@Slf4j
@NoArgsConstructor
public class PingRule implements Rule {

    private boolean isOk = true;

    @Autowired
    private EmailService emailService;

    TextField txtAddress = new TextField("Address");
    TextField txtEmail = new TextField("E-mail");

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
        return "Checks the availability of the resource and sends an email notification if it becomes unavailable.";
    }

    @Override
    public String execute(Map<String, String> parameters) {
        log.info("Executing PingRule at {}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        if (!parameters.isEmpty() && parameters.containsKey("address") && parameters.containsKey("email")) {
            try {
                String host = parameters.get("address");
                String email = parameters.get("email");
                InetAddress address = InetAddress.getByName(host);
                boolean reachable = address.isReachable(1000);

                if (!reachable) {
                    if (isOk) {
                        isOk = false;
                        emailService.sendEmail(email,
                                "Service Unavailable: " + host,
                                "<h1>Attention!</h1><p>Host '" + host + "' is not reachable</p>");
                    }
                    return "Host '" + host + "' is not reachable";
                } else {
                    if (!isOk) {
                        isOk = true;
                        emailService.sendEmail(email,
                                "Service is Back Online: " + host,
                                "<h1>Service online</h1><p>Host '" + host + "' is reachable now</p>");
                    }
                }

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
            if (parameters.containsKey("email")) txtEmail.setValue(parameters.get("email"));
        }

        components.add(txtAddress);
        components.add(txtEmail);

        return components;
    }

    @Override
    public Map<String, String> getParameters() {
        Map<String, String> map = new HashMap<>();

        map.put("address", txtAddress.getValue());
        map.put("email", txtEmail.getValue());

        return map;
    }

}