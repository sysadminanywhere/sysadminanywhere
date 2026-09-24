package com.sysadminanywhere.inventory.repository;

import com.sysadminanywhere.inventory.entity.ComputerHardware;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import com.sysadminanywhere.common.inventory.model.ComputerPatchStatus;

@Repository
public interface ComputerHardwareRepository extends JpaRepository<ComputerHardware, Long> {

    Optional<ComputerHardware> findByComputerIdAndHardwareModelId(Long computerId, Long hardwareModelId);
    
    List<ComputerHardware> findByComputerId(Long computerId);

    @Query("SELECT new com.sysadminanywhere.common.inventory.model.ComputerPatchStatus(c.name, MAX(p.propertyValue), false) "
            + "FROM ComputerHardware ch JOIN ch.computer c JOIN ch.hardwareModel hm JOIN ch.properties p "
            + "WHERE hm.hardwareType = 'Patch' AND p.propertyName = 'InstalledOn' GROUP BY c.name ORDER BY c.name")
    List<ComputerPatchStatus> findPatchStatuses();

}
