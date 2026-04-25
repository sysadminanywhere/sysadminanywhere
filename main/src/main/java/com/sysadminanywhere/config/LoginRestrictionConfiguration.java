package com.sysadminanywhere.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.login")
public class LoginRestrictionConfiguration {

    private List<String> allowedUsers = new ArrayList<>();
    private List<String> allowedGroups = new ArrayList<>();

    public boolean isUserAllowed(String username, List<String> userGroups) {
        // If no restrictions are configured, allow all users
        if (allowedUsers.isEmpty() && allowedGroups.isEmpty()) {
            return true;
        }

        // Check if user is in allowed users list
        if (!allowedUsers.isEmpty() && allowedUsers.contains(username)) {
            return true;
        }

        // Check if user has any of the allowed groups
        if (!allowedGroups.isEmpty() && userGroups != null) {
            for (String group : userGroups) {
                if (allowedGroups.contains(group)) {
                    return true;
                }
            }
        }

        // If restrictions are configured but user doesn't match, deny access
        return false;
    }
}
