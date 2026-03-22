package com.sysadminanywhere.inventory.repository;

import com.sysadminanywhere.inventory.entity.HardwareProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HardwarePropertyRepository extends JpaRepository<HardwareProperty, Long> {

}
