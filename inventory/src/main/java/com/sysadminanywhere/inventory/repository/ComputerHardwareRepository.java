package com.sysadminanywhere.inventory.repository;

import com.sysadminanywhere.inventory.entity.ComputerHardware;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import com.sysadminanywhere.common.inventory.model.ComputerPatchStatus;
import com.sysadminanywhere.common.inventory.model.HardwareComputerItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

@Repository
public interface ComputerHardwareRepository extends JpaRepository<ComputerHardware, Long> {

    Optional<ComputerHardware> findByComputerIdAndHardwareModelId(Long computerId, Long hardwareModelId);
    
    List<ComputerHardware> findByComputerId(Long computerId);

    @Query("SELECT new com.sysadminanywhere.common.inventory.model.HardwareComputerItem(c.id, c.name, c.checkingDate, c.lastScanStatus, c.lastScanError, COUNT(ch.id)) "
            + "FROM Computer c LEFT JOIN c.computerHardwares ch WHERE LOWER(c.name) LIKE LOWER(:name) "
            + "GROUP BY c.id, c.name, c.checkingDate, c.lastScanStatus, c.lastScanError ORDER BY c.name")
    Page<com.sysadminanywhere.common.inventory.model.HardwareComputerItem> findComputerHardwareSummaries(
            @Param("name") String name, Pageable pageable);

    @Query("SELECT new com.sysadminanywhere.common.inventory.model.HardwareComputerItem(c.id, c.name, c.checkingDate, c.lastScanStatus, c.lastScanError, COUNT(allHardware.id)) "
            + "FROM HardwareModel hm JOIN hm.computerHardwares selected JOIN selected.computer c LEFT JOIN c.computerHardwares allHardware "
            + "WHERE hm.id = :modelId GROUP BY c.id, c.name, c.checkingDate, c.lastScanStatus, c.lastScanError ORDER BY c.name")
    Page<HardwareComputerItem> findComputersByHardwareModelId(@Param("modelId") Long modelId, Pageable pageable);

    @Query("SELECT new com.sysadminanywhere.common.inventory.model.ComputerPatchStatus(c.name, MAX(p.propertyValue), false) "
            + "FROM ComputerHardware ch JOIN ch.computer c JOIN ch.hardwareModel hm JOIN ch.properties p "
            + "WHERE hm.hardwareType = 'Patch' AND p.propertyName = 'InstalledOn' GROUP BY c.name ORDER BY c.name")
    List<ComputerPatchStatus> findPatchStatuses();

}
