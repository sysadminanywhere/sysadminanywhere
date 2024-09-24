package com.sysadminanywhere.security;

import com.sysadminanywhere.model.Person;
import com.sysadminanywhere.service.LdapService;
import com.sysadminanywhere.service.WmiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    LdapService ldapService;

    @Autowired
    WmiService wmiService;

    @Override
    public Authentication authenticate(Authentication authentication)
            throws AuthenticationException {

        String userName = authentication.getName();
        String password = authentication.getCredentials().toString();

        Boolean result = ldapService.login(userName, password);

        if (!result) {
            throw new BadCredentialsException("Unknown user " + userName);
        }

        wmiService.init(userName, password);

        Person myUser = new Person(userName, password, "ADMIN");

        UserDetails principal = User.builder()
                .username(myUser.getLogin())
                .password(myUser.getPassword())
                .roles(myUser.getRole())
                .build();

        return new UsernamePasswordAuthenticationToken(principal, password, principal.getAuthorities());
    }
    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }

}