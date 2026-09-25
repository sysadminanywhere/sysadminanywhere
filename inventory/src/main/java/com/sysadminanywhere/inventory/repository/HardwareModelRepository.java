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
import com.sysadminanywhere.common.inventory.model.HardwareCatalogItem;

@Repository
public interface HardwareModelRepository extends JpaRepository<HardwareModel, Long> {

    @Query("SELECT new com.sysadminanywhere.common.inventory.model.HardwareItem(hm.id, hm.name, hm.hardwareType) FROM HardwareModel hm WHERE hm.hardwareType = :type AND hm.name LIKE :name")
    Page<HardwareItem> findByNameAndType(@Param("name") String name, @Param("type") String type, Pageable pageable);

    @Query("SELECT new com.sysadminanywhere.common.inventory.model.HardwareCatalogItem(hm.id, hm.name, hm.hardwareType, COUNT(DISTINCT ch.computer.id)) "
            + "FROM HardwareModel hm JOIN hm.computerHardwares ch WHERE LOWER(hm.name) LIKE LOWER(:name) "
            + "AND (:type IS NULL OR hm.hardwareType = :type) AND hm.hardwareType <> 'Patch' AND hm.hardwareType <> 'OperatingSystem' "
            + "GROUP BY hm.id, hm.name, hm.hardwareType ORDER BY hm.hardwareType, hm.name")
    Page<HardwareCatalogItem> findHardwareCatalog(@Param("name") String name, @Param("type") String type, Pageable pageable);

    @Query("SELECT new com.sysadminanywhere.common.inventory.model.HardwareCatalogItem(hm.id, hm.name, hm.hardwareType, COUNT(DISTINCT ch.computer.id)) "
            + "FROM HardwareModel hm JOIN hm.computerHardwares ch WHERE hm.id = :id "
            + "GROUP BY hm.id, hm.name, hm.hardwareType")
    Optional<HardwareCatalogItem> findHardwareCatalogItem(@Param("id") Long id);

    Optional<HardwareModel> findByNameAndHardwareType(String name, String hardwareType);

    @Query("SELECT hm.name, COUNT(DISTINCT ch.computer.id) FROM ComputerHardware ch JOIN ch.hardwareModel hm WHERE hm.hardwareType = 'OperatingSystem' GROUP BY hm.name ORDER BY hm.name")
    List<Object[]> findOperatingSystemCounts();

    @Query("SELECT COUNT(DISTINCT ch.computer.id) FROM ComputerHardware ch JOIN ch.hardwareModel hm WHERE hm.hardwareType = :type")
    long countComputersWithHardwareType(@Param("type") String type);

}
