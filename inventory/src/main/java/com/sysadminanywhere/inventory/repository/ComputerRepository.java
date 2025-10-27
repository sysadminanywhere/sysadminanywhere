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

    // select c."name", c.dns from computers c inner join installations i on c.id = i.computer_id where i.software_id = 1
    @Query("SELECT new com.sysadminanywhere.model.ComputerItem(c.id, c.name, c.dns) FROM Computer c JOIN c.installations i WHERE i.software.id = :softwareId and c.name LIKE :name")
    Page<ComputerItem> getComputersWithSoftware(@Param("softwareId") Long softwareId, @Param("name") String name, Pageable pageable);

}
