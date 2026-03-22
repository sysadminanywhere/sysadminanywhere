package com.sysadminanywhere.inventory.repository;

import com.sysadminanywhere.inventory.entity.HardwareType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HardwareTypeRepository extends JpaRepository<HardwareType, Long> {
    
    Optional<HardwareType> findByName(String name);
    
    boolean existsByName(String name);
    
}
