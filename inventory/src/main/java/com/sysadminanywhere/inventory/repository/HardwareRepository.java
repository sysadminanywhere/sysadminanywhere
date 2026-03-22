package com.sysadminanywhere.inventory.repository;

import com.sysadminanywhere.common.inventory.model.HardwareCount;
import com.sysadminanywhere.inventory.entity.Hardware;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HardwareRepository extends JpaRepository<Hardware, Long> {
    List<Hardware> findByNameAndType(String name, String type);
    
    @Query("SELECT h FROM Hardware h WHERE " +
            "(:name = '' OR LOWER(h.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "LOWER(h.type) = LOWER(:type)")
    Page<Hardware> searchHardware(String name, String type, Pageable pageable);

    @Query(value = "SELECT h.id, h.name, h.type, COUNT(ch.id) " +
            "FROM Hardware h LEFT JOIN ComputerHardware ch ON h.id = ch.hardware.id " +
            "WHERE (:name IS NULL OR LOWER(h.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:type IS NULL OR LOWER(h.type) LIKE LOWER(CONCAT('%', :type, '%'))) " +
            "GROUP BY h.id, h.name, h.type", 
            countQuery = "SELECT COUNT(DISTINCT h.id) " +
            "FROM Hardware h LEFT JOIN ComputerHardware ch ON h.id = ch.hardware.id " +
            "WHERE (:name IS NULL OR LOWER(h.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:type IS NULL OR LOWER(h.type) LIKE LOWER(CONCAT('%', :type, '%')))")
    Page<Object[]> getHardwareInstallationCount(String name, String type, Pageable pageable);
}
