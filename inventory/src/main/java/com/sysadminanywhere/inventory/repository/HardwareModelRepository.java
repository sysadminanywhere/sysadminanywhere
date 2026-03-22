package com.sysadminanywhere.inventory.repository;

import com.sysadminanywhere.inventory.entity.HardwareModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HardwareModelRepository extends JpaRepository<HardwareModel, Long> {
}
