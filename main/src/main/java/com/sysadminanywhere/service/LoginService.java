package com.sysadminanywhere.service;

import com.sysadminanywhere.client.directory.UsersServiceClient;
import com.sysadminanywhere.common.directory.model.UserEntry;
import com.sysadminanywhere.entity.LoginEntity;
import com.sysadminanywhere.repository.LoginRepository;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class LoginService {

    private final UsersServiceClient usersServiceClient;

    private final LoginRepository loginRepository;
    boolean isLoggedIn = false;

    public LoginService(UsersServiceClient usersServiceClient, LoginRepository loginRepository) {
        this.usersServiceClient = usersServiceClient;
        this.loginRepository = loginRepository;
    }

    public LoginEntity Login(UserEntry user) {
        Optional<LoginEntity> login = loginRepository.findByUserName(user.getCn());
        LoginEntity result;

        if (login.isEmpty()) {
            LoginEntity loginEntity = new LoginEntity();
            loginEntity.setDisplayName(user.getDisplayName());
            loginEntity.setUserName(user.getCn());
            loginEntity.setLastLogin(LocalDateTime.now());
            result = loginRepository.save(loginEntity);
        } else {
            LoginEntity loginEntity = login.get();
            loginEntity.setLastLogin(LocalDateTime.now());
            result = loginRepository.save(loginEntity);
        }

        isLoggedIn = true;

        return result;
    }

    public Optional<LoginEntity> getLogin(UserEntry user) {
        Optional<LoginEntity> login = loginRepository.findByUserName(user.getCn());
        return login;
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

}