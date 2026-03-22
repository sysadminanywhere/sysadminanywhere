package com.sysadminanywhere.inventory.repository;

import com.sysadminanywhere.inventory.entity.HardwareValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HardwareValueRepository extends JpaRepository<HardwareValue, Long> {
}
