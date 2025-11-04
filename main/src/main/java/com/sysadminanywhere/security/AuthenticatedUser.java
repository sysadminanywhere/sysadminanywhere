package com.sysadminanywhere.security;

import com.sysadminanywhere.common.directory.model.UserEntry;
import com.sysadminanywhere.service.UsersService;
import com.vaadin.flow.spring.security.AuthenticationContext;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class AuthenticatedUser {

    private final UsersService usersService;
    private final AuthenticationContext authenticationContext;

    public AuthenticatedUser(AuthenticationContext authenticationContext, UsersService usersService) {
        this.usersService = usersService;
        this.authenticationContext = authenticationContext;
    }

    @Transactional
    public Optional<UserEntry> getUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof OidcUser user) {
            String userName = user.getClaim("preferred_username");
            String federationSource = user.getClaim("federation_source");
            if (federationSource != null) {
                try {
                    return Optional.ofNullable(usersService.getByCN(userName));
                } catch (Exception ex) {
                    log.error("Domain service is unavailable!");
                    return Optional.empty();
                }
            }
        }
        return Optional.empty();
    }

    public void logout() {
        authenticationContext.logout();
    }

}