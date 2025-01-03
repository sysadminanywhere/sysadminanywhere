package com.sysadminanywhere.repository;

import com.sysadminanywhere.entity.LoginEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LoginRepository extends JpaRepository<LoginEntity, Long> {

    Optional<LoginEntity> findByObjectGuid(UUID objectGuid);

}