package com.sysadminanywhere.model.monitoring;

import com.sysadminanywhere.model.wmi.EventEntity;
import com.sysadminanywhere.service.EmailService;
import com.sysadminanywhere.service.WmiResolveService;
import com.sysadminanywhere.service.WmiService;
import com.vaadin.flow.component.textfield.TextField;
import lombok.extern.slf4j.Slf4j;
import org.sentrysoftware.wmi.exceptions.WmiComException;
import org.sentrysoftware.wmi.exceptions.WqlQuerySyntaxException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

@Component
@Scope("prototype")
@Slf4j
public class IncorrectPasswordEntryRule implements Rule {

    private LocalDateTime date = LocalDateTime.now();

    @Value("${ldap.host.username:}")
    String userName;

    @Value("${ldap.host.password:}")
    String password;

    @Autowired
    private EmailService emailService;

    private WmiService wmiService;

    TextField txtUser = new TextField("User");
    TextField txtEmail = new TextField("E-mail");

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
    public String execute(Map<String, String> parameters) {
        log.info("Executing IncorrectPasswordEntryRule at {}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        if (!parameters.isEmpty() && parameters.containsKey("address") && parameters.containsKey("email")) {
            String user = parameters.get("user");
            String email = parameters.get("email");

            try {

                wmiService = new WmiService();
                wmiService.init(userName, password);

                WmiResolveService<EventEntity> wmiResolveService = new WmiResolveService<>(EventEntity.class);

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss.SSSSSSXXX");

                LocalDateTime dateTime = LocalDateTime.now();

                String startDate = date.format(formatter);
                String endDate = dateTime.format(formatter);

                List<EventEntity> result = wmiResolveService.GetValues(wmiService.execute("", "SELECT * FROM Win32_NTLogEvent WHERE Logfile = 'Security' AND EventCode = '4625' AND Message LIKE '" + user + "' Where TimeGenerated >= '" + startDate + "' AND TimeGenerated <= '" + endDate + "'"));

                date = dateTime;

                if (!result.isEmpty()) {
                    EventEntity entity = result.get(0);

                    emailService.sendEmail(email,
                            "Failed Login Attempt",
                            "<h1>Incorrect password entry!</h1><p>User " + user + " entered an incorrect password.</p><p>Details: <br>" + entity.getMessage() + "</p>");
                }

            } catch (Exception e) {
                return "Error: " + e.getMessage();
            } finally {
                wmiService = null;
            }

            return "";
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
            if (parameters.containsKey("user")) txtUser.setValue(parameters.get("user"));
            if (parameters.containsKey("email")) txtEmail.setValue(parameters.get("email"));
        }

        components.add(txtUser);
        components.add(txtEmail);

        return components;
    }

    @Override
    public Map<String, String> getParameters() {
        Map<String, String> map = new HashMap<>();

        map.put("user", txtUser.getValue());
        map.put("email", txtEmail.getValue());

        return map;
    }

}