package com.sysadminanywhere.inventory.repository;

import com.sysadminanywhere.inventory.entity.InventoryScanRun;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InventoryScanRunRepository extends JpaRepository<InventoryScanRun, Long> {
    List<InventoryScanRun> findTop20ByOrderByStartedAtDesc();
}
