package com.sysadminanywhere.inventory.repository;

import com.sysadminanywhere.common.inventory.model.HardwareItem;
import com.sysadminanywhere.inventory.entity.HardwareModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface HardwareModelRepository extends JpaRepository<HardwareModel, Long> {

    @Query("SELECT new com.sysadminanywhere.common.inventory.model.HardwareItem(hm.id, hm.name, hm.hardwareType) FROM HardwareModel hm WHERE hm.hardwareType = :type AND hm.name LIKE :name")
    Page<HardwareItem> findByNameAndType(@Param("name") String name, @Param("type") String type, Pageable pageable);

    Optional<HardwareModel> findByNameAndHardwareType(String name, String hardwareType);

    @Query("SELECT hm.name, COUNT(DISTINCT ch.computer.id) FROM ComputerHardware ch JOIN ch.hardwareModel hm WHERE hm.hardwareType = 'OperatingSystem' GROUP BY hm.name ORDER BY hm.name")
    List<Object[]> findOperatingSystemCounts();

    @Query("SELECT COUNT(DISTINCT ch.computer.id) FROM ComputerHardware ch JOIN ch.hardwareModel hm WHERE hm.hardwareType = :type")
    long countComputersWithHardwareType(@Param("type") String type);

}
