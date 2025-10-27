package com.sysadminanywhere.inventory.repository;

import com.sysadminanywhere.inventory.entity.ComputerHardware;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ComputerHardwareRepository  extends JpaRepository<ComputerHardware, Long> {
}
