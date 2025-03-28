package com.sysadminanywhere.model.monitoring;

import com.sysadminanywhere.domain.DirectorySetting;
import com.sysadminanywhere.service.EmailService;
import com.sysadminanywhere.service.LdapService;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import lombok.extern.slf4j.Slf4j;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.directory.ldap.client.api.LdapConnectionConfig;
import org.apache.directory.ldap.client.api.LdapNetworkConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Scope("prototype")
@Slf4j
public class PasswordExpirationRule implements Rule {

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

    NumberField numberField = new NumberField("Start notifying for");
    TextField txtSubject = new TextField("Subject");
    TextArea txtMessage = new TextArea("Message");

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
    public String execute(Map<String, String> parameters) {
        log.info("Executing PasswordExpirationRule at {}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        if (!parameters.isEmpty()
                && parameters.containsKey("days")
                && parameters.containsKey("subject")
                && parameters.containsKey("message")) {

            LdapConnection connection = new LdapNetworkConnection(ldapConnectionConfig);
            ldapService = new LdapService(connection, directorySetting);

            Boolean result = ldapService.login(userName, password);

            if (!result) {
                return "Unknown user: " + userName;
            }


            double doubleValue = Double.parseDouble(parameters.get("days"));
            Long warningDays = (long) doubleValue;
            String subject = parameters.get("subject");
            String message = parameters.get("message");

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime warningDate = now.plusDays(warningDays);

            List<Entry> list = ldapService.search("(&(objectClass=user)(objectCategory=person))");

            long n = 0;

            for (Entry entry : list) {
                if (entry.containsAttribute("accountExpires")) {
                    long expiryFileTime = Long.parseLong(entry.get("accountExpires").get().getString());
                    LocalDateTime expiryDate = fileTimeToLocalDateTime(expiryFileTime);

                    if (expiryDate.isBefore(warningDate)) {
                        String email = entry.get("userPrincipalName").get().getString();
                        sendEmail(email, subject, message, expiryDate);
                        n++;
                    }
                }
            }

            ldapService = null;

            if (n > 0)
                return "Sent " + n + " password expiration notifications";
        }

        return "";
    }

        /*

     ┌───────────── second (0-59)
     │ ┌───────────── minute (0 - 59)
     │ │ ┌───────────── hour (0 - 23)
     │ │ │ ┌───────────── day of the month (1 - 31)
     │ │ │ │ ┌───────────── month (1 - 12) (or JAN-DEC)
     │ │ │ │ │ ┌───────────── day of the week (0 - 7)
     │ │ │ │ │ │          (0 or 7 is Sunday, or MON-SUN)
     │ │ │ │ │ │
     * * * * * *

    "0 0 12 * * *" every day at 12:00

    */

    @Override
    public String getDefaultCron() {
        return "0 0 10 * * *";
    }

    @Override
    public List<com.vaadin.flow.component.Component> getControls(Map<String, String> parameters) {
        List<com.vaadin.flow.component.Component> components = new ArrayList<>();

        numberField.setValue(7.0);
        txtSubject.setValue("Your account password is set to expire soon");
        txtMessage.setValue("Your account password is set to expire soon. To avoid any disruptions, please update your password before {}.\n" +
                "\n" +
                "To change your password, please follow this link: [Password Reset Link] or contact IT support if you need assistance.\n" +
                "\n" +
                "Best regards,\n" +
                "[Your Company/IT Support Team]");

        if (!parameters.isEmpty()) {
            if (parameters.containsKey("days")) numberField.setValue(Double.valueOf(parameters.get("days")));
            if (parameters.containsKey("subject")) txtSubject.setValue(parameters.get("subject"));
            if (parameters.containsKey("message")) txtMessage.setValue(parameters.get("message"));
        }

        components.add(numberField);
        components.add(txtSubject);
        components.add(txtMessage);

        return components;
    }

    @Override
    public Map<String, String> getParameters() {
        Map<String, String> map = new HashMap<>();

        map.put("days", numberField.getValue().toString());
        map.put("subject", txtSubject.getValue());
        map.put("message", txtMessage.getValue());

        return map;
    }

    private static LocalDateTime fileTimeToLocalDateTime(long fileTime) {
        long unixTime = (fileTime - 116444736000000000L) / 10000000L;
        return Instant.ofEpochSecond(unixTime).atZone(ZoneOffset.UTC).toLocalDateTime();
    }

    private void sendEmail(String email, String subject, String message, LocalDateTime expiryDate) {
        emailService.sendEmail(email,
                subject,
                MessageFormat.format(message, expiryDate));
    }

}