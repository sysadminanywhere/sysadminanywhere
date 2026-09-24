package com.sysadminanywhere.inventory.repository;

import com.sysadminanywhere.common.inventory.model.SoftwareCount;
import com.sysadminanywhere.common.inventory.model.SoftwareOnComputer;
import com.sysadminanywhere.inventory.entity.Software;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SoftwareRepository extends JpaRepository<Software, Long> {

    @Query("select count(s) from Software s where s.version is null or trim(s.version) = ''")
    long countWithoutVersion();

    @Query("select count(i) from Installation i join i.software s where s.name = :name and s.vendor = :vendor and (:version is null or s.version = :version)")
    long countInstallations(@Param("name") String name, @Param("vendor") String vendor, @Param("version") String version);

    List<Software> findByNameAndVendor(String name, String vendor);

    List<Software> findByNameAndVendorAndVersion(String name, String vendor, String version);

    @Query("select new com.sysadminanywhere.common.inventory.model.SoftwareCount(s.id, s.name, s.vendor, s.version, count(i)) from Software s JOIN s.installations i WHERE s.name LIKE :name and s.vendor LIKE :vendor GROUP BY s.id, s.name, s.vendor, s.version HAVING (:minCount is null or count(i) >= :minCount) and (:maxCount is null or count(i) <= :maxCount) order by s.name")
    Page<SoftwareCount> getSoftwareInstallationCount(@Param("name") String name, @Param("vendor") String vendor,
                                                      @Param("minCount") Long minCount, @Param("maxCount") Long maxCount,
                                                      Pageable pageable);

    @Query("select new com.sysadminanywhere.common.inventory.model.SoftwareOnComputer(s.name, s.vendor, s.version, i.installDate, null) FROM Installation i JOIN i.software s WHERE i.computer.id = :computerId order by s.name")
    Page<SoftwareOnComputer> getSoftwareOnComputer(@Param("computerId") Long computerId, Pageable pageable);

}
