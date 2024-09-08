package com.sysadminanywhere.security;

import java.util.ArrayList;
import java.util.List;

import com.sysadminanywhere.model.UserEntry;
import com.sysadminanywhere.services.LdapService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final LdapService ldapService;

    public UserDetailsServiceImpl(LdapService ldapService) {
        this.ldapService = ldapService;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntry user = ldapService.me();
        if (user == null) {
            throw new UsernameNotFoundException("No user present with username: " + username);
        } else {
            return new org.springframework.security.core.userdetails.User("admin", new BCryptPasswordEncoder().encode("admin"),
                    getAuthorities(user));
        }
    }

    private static List<GrantedAuthority> getAuthorities(UserEntry user) {
        List<GrantedAuthority> list = new ArrayList<>();
        list.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        return list;
    }

}
