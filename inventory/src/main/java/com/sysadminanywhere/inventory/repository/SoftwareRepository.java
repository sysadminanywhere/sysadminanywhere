package com.sysadminanywhere.inventory.repository;

import com.sysadminanywhere.inventory.controller.dto.SoftwareOnComputer;
import com.sysadminanywhere.inventory.entity.Software;
import com.sysadminanywhere.inventory.controller.dto.SoftwareCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface SoftwareRepository extends JpaRepository<Software, Long> {

    List<Software> findByNameAndVendorAndVersion(String name, String vendor, String version);

    @Query("select new com.sysadminanywhere.inventory.controller.dto.SoftwareCount(s.id, s.name, s.vendor, s.version, count(i)) from Software s JOIN s.installations i WHERE s.name LIKE :name and s.vendor LIKE :vendor GROUP BY s.id, s.name, s.vendor, s.version order by s.name")
    Page<SoftwareCount> getSoftwareInstallationCount(@Param("name") String name, @Param("vendor") String vendor, Pageable pageable);

    @Query("select new com.sysadminanywhere.inventory.controller.dto.SoftwareOnComputer(s.name, s.vendor, s.version, i.installDate, i.checkingDate) FROM Installation i JOIN i.software s WHERE i.computer.id = :computerId order by s.name")
    Page<SoftwareOnComputer> getSoftwareOnComputer(@Param("computerId") Long computerId, Pageable pageable);

}
