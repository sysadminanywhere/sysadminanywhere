package com.sysadminanywhere.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class CustomOidcUserService extends OidcUserService {

    @Value("${ldap.host.groups.admins:}")
    private String admins;

    @Value("${ldap.host.groups.users:}")
    private String users;

    private Map<String, String> groupRoleMap;

    @PostConstruct
    public void initGroupMap() {
        groupRoleMap = new HashMap<>();

        if (admins != null && !admins.isBlank()) {
            groupRoleMap.put(admins, "admins");
        }
        if (users != null && !users.isBlank()) {
            groupRoleMap.put(users, "users");
        }
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);
        return enhanceUser(oidcUser);
    }

    private OidcUser enhanceUser(OidcUser oidcUser) {
        Set<GrantedAuthority> authorities = new HashSet<>(oidcUser.getAuthorities());

        List<String> groups = oidcUser.getClaimAsStringList("groups");

        if (groups != null) {
            groups.stream()
                    .map(groupRoleMap::get)
                    .filter(Objects::nonNull)
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .forEach(authorities::add);
        }

        return new DefaultOidcUser(authorities, oidcUser.getIdToken(), oidcUser.getUserInfo());
    }

}