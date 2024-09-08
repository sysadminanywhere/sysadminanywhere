package com.sysadminanywhere.security;

import com.sysadminanywhere.model.UserEntry;
import com.sysadminanywhere.services.LdapService;
import com.vaadin.flow.spring.security.AuthenticationContext;
import java.util.Optional;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AuthenticatedUser {

    private final LdapService ldapService;
    private final AuthenticationContext authenticationContext;

    public AuthenticatedUser(AuthenticationContext authenticationContext, LdapService ldapService) {
        this.ldapService = ldapService;
        this.authenticationContext = authenticationContext;
    }

    @Transactional
    public Optional<UserEntry> get() {
        return authenticationContext.getAuthenticatedUser(UserDetails.class)
                .map(userDetails -> ldapService.me());
    }

    public void logout() {
        authenticationContext.logout();
    }

}
