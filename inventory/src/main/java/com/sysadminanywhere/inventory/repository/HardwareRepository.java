package com.sysadminanywhere.inventory.repository;

import com.sysadminanywhere.inventory.entity.Hardware;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HardwareRepository  extends JpaRepository<Hardware, Long> {
}
