package com.sysadminanywhere.repository;

import com.sysadminanywhere.entity.LoginEntity;
import com.sysadminanywhere.entity.SettingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SettingsRepository extends JpaRepository<SettingEntity, Long> {

    Optional<SettingEntity> findByLogin(LoginEntity login);

}