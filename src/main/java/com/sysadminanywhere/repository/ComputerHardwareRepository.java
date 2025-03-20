package com.sysadminanywhere.repository;

import com.sysadminanywhere.entity.ComputerHardware;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ComputerHardwareRepository  extends JpaRepository<ComputerHardware, Long> {
}
