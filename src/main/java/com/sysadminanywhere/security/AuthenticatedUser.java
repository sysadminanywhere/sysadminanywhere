package com.sysadminanywhere.security;

import com.sysadminanywhere.model.ad.UserEntry;
import com.sysadminanywhere.service.UsersService;
import com.vaadin.flow.spring.security.AuthenticationContext;
import java.util.Optional;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AuthenticatedUser {

    private final UsersService usersService;
    private final AuthenticationContext authenticationContext;

    public AuthenticatedUser(AuthenticationContext authenticationContext, UsersService usersService) {
        this.usersService = usersService;
        this.authenticationContext = authenticationContext;
    }

    @Transactional
    public Optional<UserEntry> get() {
        SecurityContext context = SecurityContextHolder.getContext();
        Object principal = context.getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) context.getAuthentication().getPrincipal();
            return Optional.ofNullable(usersService.getByCN(userDetails.getUsername()));
        }
        // Anonymous or no authentication.
        return Optional.empty();    }

    public void logout() {
        authenticationContext.logout();
    }

}
