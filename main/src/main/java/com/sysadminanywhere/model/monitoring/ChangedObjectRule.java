package com.sysadminanywhere.model.monitoring;

import com.sysadminanywhere.common.directory.dto.AuditDto;
import com.sysadminanywhere.domain.DirectorySetting;
import com.sysadminanywhere.service.EmailService;
import com.sysadminanywhere.service.LdapService;
import com.vaadin.flow.component.textfield.TextField;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.directory.ldap.client.api.LdapConnectionConfig;
import org.apache.directory.ldap.client.api.LdapNetworkConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
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
public class ChangedObjectRule implements Rule {

    LocalDateTime whenChanged;

    @Value("${ldap.host.username:}")
    String userName;

    @Value("${ldap.host.password:}")
    String password;

    @Autowired
    private LdapConnectionConfig ldapConnectionConfig;

    @Autowired
    DirectorySetting directorySetting;

    @Autowired
    private EmailService emailService;

    private LdapService ldapService;

    TextField txtUser = new TextField("CN");
    TextField txtEmail = new TextField("E-mail");

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
        return "Notifies that the specified domain object has been modified.";
    }

    @Override
    public String execute(Map<String, String> parameters) {
        log.info("Executing ChangedObjectRule at {}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        if (!parameters.isEmpty() && parameters.containsKey("user") && parameters.containsKey("email")) {
            String user = parameters.get("user");
            String email = parameters.get("email");

            if (whenChanged == null)
                whenChanged = LocalDate.now().atStartOfDay();

            LdapConnection connection = new LdapNetworkConnection(ldapConnectionConfig);
            ldapService = new LdapService(connection, directorySetting);

            Boolean result = ldapService.login(userName, password);

            if (!result) {
                return "Unknown user: " + userName;
            }

            Map<String, Object> filters = new HashMap<>();
            filters.put("startDate", LocalDate.now());

//            List<AuditDto> list = ldapService.getAuditList(filters);
//
//            for (AuditDto item : list) {
//                if (item.getName().equalsIgnoreCase(user) && item.getWhenChanged().isAfter(whenChanged)) {
//                    emailService.sendEmail(email,
//                            "Object changed",
//                            "<h1>Object changed!</h1><p>Object " + user + " has been modified.</p>");
//
//                    whenChanged = item.getWhenChanged();
//                }
//            }
        }

        ldapService = null;

        return "";
    }

    @Override
    public String getDefaultCron() {
        return "0 * * * * *";
    }

    @Override
    public List<com.vaadin.flow.component.Component> getControls(Map<String, String> parameters) {
        List<com.vaadin.flow.component.Component> components = new ArrayList<>();

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