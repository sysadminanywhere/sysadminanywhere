package com.sysadminanywhere.inventory.repository;

import com.sysadminanywhere.common.inventory.model.ComputerItem;
import com.sysadminanywhere.inventory.entity.Computer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComputerRepository extends JpaRepository<Computer, Long>, JpaSpecificationExecutor<Computer> {

    List<Computer> findAllByName(String cn);

    // select c."name" from computers c inner join installations i on c.id = i.computer_id where i.software_id = 1
    @Query("SELECT new com.sysadminanywhere.common.inventory.model.ComputerItem(c.id, c.name, null) FROM Computer c JOIN c.installations i WHERE i.software.id = :softwareId and c.name LIKE :name")
    Page<ComputerItem> getComputersWithSoftware(@Param("softwareId") Long softwareId, @Param("name") String name, Pageable pageable);

    // select c."name", ch.checking_date from computers c inner join computer_hardware ch on c.id = ch.computer_id where ch.hardware_type_id = 1
    @Query("SELECT new com.sysadminanywhere.common.inventory.model.ComputerItem(c.id, c.name, ch.checkingDate) FROM Computer c JOIN c.computerHardwares ch WHERE ch.hardwareModel.hardwareType = :hardwareTypeId and c.name LIKE :name")
    Page<ComputerItem> getComputersWithHardware(@Param("hardwareTypeId") String hardwareTypeId, @Param("name") String name, Pageable pageable);

    // select distinct c."name", max(ch.checking_date) from computers c inner join computer_hardware ch on c.id = ch.computer_id group by c.id, c."name"
    @Query("SELECT new com.sysadminanywhere.common.inventory.model.ComputerItem(c.id, c.name, MAX(ch.checkingDate)) FROM Computer c JOIN c.computerHardwares ch WHERE c.name LIKE :name GROUP BY c.id, c.name")
    Page<ComputerItem> getAllComputersWithHardware(@Param("name") String name, Pageable pageable);

}
