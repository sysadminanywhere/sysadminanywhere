package com.sysadminanywhere.inventory.repository;

import com.sysadminanywhere.inventory.entity.Computer;
import com.sysadminanywhere.inventory.entity.ComputerHardware;
import com.sysadminanywhere.inventory.entity.Hardware;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComputerHardwareRepository  extends JpaRepository<ComputerHardware, Long> {
    List<ComputerHardware> findAllByComputer(Computer computer);
    List<ComputerHardware> findAllByComputerAndHardware(Computer computer, Hardware hardware);
    
    @Query("SELECT ch FROM ComputerHardware ch JOIN FETCH ch.hardware JOIN FETCH ch.computer WHERE ch.computer = :computer")
    List<ComputerHardware> findAllByComputerWithHardware(Computer computer);
}
