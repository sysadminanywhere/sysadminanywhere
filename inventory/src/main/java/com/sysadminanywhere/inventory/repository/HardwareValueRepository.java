package com.sysadminanywhere.inventory.repository;

import com.sysadminanywhere.inventory.entity.Computer;
import com.sysadminanywhere.inventory.entity.HardwareValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HardwareValueRepository extends JpaRepository<HardwareValue, Long> {

    Optional<HardwareValue> findByComputerAndPropertyValue(Computer computer, String propertyValue);

}
