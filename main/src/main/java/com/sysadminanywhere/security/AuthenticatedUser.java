package com.sysadminanywhere.security;

import com.sysadminanywhere.entity.User;
import com.sysadminanywhere.repository.UserRepository;
import com.vaadin.flow.spring.security.AuthenticationContext;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class AuthenticatedUser {

    private final UserRepository userRepository;
    private final AuthenticationContext authenticationContext;

    public AuthenticatedUser(AuthenticationContext authenticationContext, UserRepository userRepository) {
        this.userRepository = userRepository;
        this.authenticationContext = authenticationContext;
    }

    public void save(String username) {
        Optional<User> user = userRepository.findByUsername(username);
        if (!user.isPresent()) {
            User newUser = new User();
            newUser.setUsername(username);
            userRepository.save(newUser);
        }
    }

    @Transactional
    public Optional<User> get() {
        Optional<String> username = authenticationContext.getAuthenticatedUser(String.class);
        if (username.isEmpty()) {
            return Optional.empty();
        } else {
            return userRepository.findByUsername(username.get());
        }
    }

    public void logout() {
        authenticationContext.logout();
    }

}