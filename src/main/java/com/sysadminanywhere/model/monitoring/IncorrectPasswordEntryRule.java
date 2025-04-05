package com.sysadminanywhere.model.monitoring;

import com.sysadminanywhere.model.wmi.EventEntity;
import com.sysadminanywhere.service.EmailService;
import com.sysadminanywhere.service.WmiResolveService;
import com.sysadminanywhere.service.WmiService;
import com.vaadin.flow.component.textfield.TextField;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
@Scope("prototype")
@Slf4j
@NoArgsConstructor
public class IncorrectPasswordEntryRule implements Rule {

    @Value("${ldap.host.username:}")
    String userName;

    @Value("${ldap.host.password:}")
    String password;

    Long lastRecordNumber = 0L;

    @Autowired
    private EmailService emailService;

    private WmiService wmiService;

    TextField txtHost = new TextField("Host");
    TextField txtUser = new TextField("CN");
    TextField txtEmail = new TextField("E-mail");

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
        return "Notifies that the specified user entered an incorrect password. Relevant for system passwords.";
    }

    @Override
    public String execute(Map<String, String> parameters) {
        log.info("Executing IncorrectPasswordEntryRule at {}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        if (!parameters.isEmpty() && parameters.containsKey("user") && parameters.containsKey("email") && parameters.containsKey("host")) {
            String user = parameters.get("user");
            String email = parameters.get("email");
            String host = parameters.get("host");

            try {

                wmiService = new WmiService();
                wmiService.init(userName, password);

                WmiResolveService<EventEntity> wmiResolveService = new WmiResolveService<>(EventEntity.class);

                LocalDate date = LocalDate.now();

                String startDate = date.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "000000.000000000";
                String endDate = date.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "235959.000000000";

                List<EventEntity> result = wmiResolveService.GetValues(wmiService.execute(host,
                        "SELECT * FROM Win32_NTLogEvent WHERE Logfile = 'Security' AND EventCode = '4625' AND TimeGenerated >= '" + startDate + "' AND TimeGenerated <= '" + endDate + "'"));

                if (!result.isEmpty()) {
                    result.sort(Comparator.comparing(EventEntity::getRecordNumber).reversed());
                    for (EventEntity event : result) {
                        if (event.getRecordNumber() > lastRecordNumber) {

                            for (String value : event.getInsertionStrings()) {
                                if (value.equalsIgnoreCase(user)){
                                    emailService.sendEmail(email,
                                            "Failed Login Attempt",
                                            "<h1>Incorrect password entry!</h1><p>User " + user + " entered an incorrect password.</p><p>Details: <br><pre>" + event.getMessage() + "</pre></p>");
                                }
                            }
                        }

                    }
                    lastRecordNumber = result.get(0).getRecordNumber();
                }

            } catch (Exception e) {
                return "Error: " + e.getMessage();
            } finally {
                wmiService = null;
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
            if (parameters.containsKey("user")) txtUser.setValue(parameters.get("user"));
            if (parameters.containsKey("email")) txtEmail.setValue(parameters.get("email"));
            if (parameters.containsKey("host")) txtHost.setValue(parameters.get("host"));
        }

        components.add(txtHost);
        components.add(txtUser);
        components.add(txtEmail);

        return components;
    }

    @Override
    public Map<String, String> getParameters() {
        Map<String, String> map = new HashMap<>();

        map.put("user", txtUser.getValue());
        map.put("email", txtEmail.getValue());
        map.put("host", txtHost.getValue());

        return map;
    }

}