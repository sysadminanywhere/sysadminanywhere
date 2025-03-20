package com.sysadminanywhere.repository;

import com.sysadminanywhere.entity.Computer;
import com.sysadminanywhere.entity.Installation;
import com.sysadminanywhere.entity.Software;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InstallationRepository  extends JpaRepository<Installation, Long> {

    List<Installation> findAllByComputerAndSoftware(Computer computer, Software software);
    List<Installation> findAllByComputer(Computer computer);

}