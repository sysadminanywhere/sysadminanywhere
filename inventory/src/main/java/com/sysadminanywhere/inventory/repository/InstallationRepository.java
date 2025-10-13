package com.sysadminanywhere.inventory.repository;

import com.sysadminanywhere.inventory.entity.Computer;
import com.sysadminanywhere.inventory.entity.Installation;
import com.sysadminanywhere.inventory.entity.Software;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InstallationRepository  extends JpaRepository<Installation, Long> {

    List<Installation> findAllByComputerAndSoftware(Computer computer, Software software);

    List<Installation> findAllByComputer(Computer computer);

}