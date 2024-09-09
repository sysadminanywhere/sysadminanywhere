package com.sysadminanywhere.security;

import com.sysadminanywhere.model.UserEntry;
import com.sysadminanywhere.services.LdapService;
import com.sysadminanywhere.services.UserService;
import com.vaadin.flow.spring.security.AuthenticationContext;
import java.util.Optional;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AuthenticatedUser {

    private final UserService userService;
    private final AuthenticationContext authenticationContext;

    public AuthenticatedUser(AuthenticationContext authenticationContext, UserService userService) {
        this.userService = userService;
        this.authenticationContext = authenticationContext;
    }

    @Transactional
    public Optional<UserEntry> get() {
        SecurityContext context = SecurityContextHolder.getContext();
        Object principal = context.getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) context.getAuthentication().getPrincipal();
            return Optional.ofNullable(userService.getByCN(userDetails.getUsername()));
        }
        // Anonymous or no authentication.
        return Optional.empty();    }

    public void logout() {
        authenticationContext.logout();
    }

}
