package com.sysadminanywhere.inventory.repository;

import com.sysadminanywhere.inventory.entity.ComputerHardware;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ComputerHardwareRepository extends JpaRepository<ComputerHardware, Long> {

    Optional<ComputerHardware> findByComputerIdAndHardwareModelId(Long computerId, Long hardwareModelId);
    
    List<ComputerHardware> findByComputerId(Long computerId);

}