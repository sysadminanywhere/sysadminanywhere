package com.sysadminanywhere.service;

import com.sysadminanywhere.entity.LoginEntity;
import com.sysadminanywhere.model.ad.UserEntry;
import com.sysadminanywhere.repository.LoginRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class LoginService {

    private final LoginRepository loginRepository;
    boolean isLoggedIn = false;

    public LoginService(LoginRepository loginRepository) {
        this.loginRepository = loginRepository;
    }

    public LoginEntity Login(UserEntry user) {
        Optional<LoginEntity> login = loginRepository.findByObjectGuid(user.getObjectGUID());
        LoginEntity result;

        if (login.isEmpty()) {
            LoginEntity loginEntity = new LoginEntity();
            loginEntity.setDisplayName(user.getDisplayName());
            loginEntity.setObjectGuid(user.getObjectGUID());
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
        Optional<LoginEntity> login = loginRepository.findByObjectGuid(user.getObjectGUID());
        return login;
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

}