package com.sysadminanywhere.inventory.repository;

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
    
    @Query("SELECT DISTINCT h.type FROM Hardware h")
    List<String> findAllHardwareTypes();
    
    @Query("SELECT h FROM Hardware h WHERE " +
            "(:name IS NULL OR LOWER(h.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:type IS NULL OR LOWER(h.type) LIKE LOWER(CONCAT('%', :type, '%')))")
    Page<Hardware> searchHardware(String name, String type, Pageable pageable);
}
