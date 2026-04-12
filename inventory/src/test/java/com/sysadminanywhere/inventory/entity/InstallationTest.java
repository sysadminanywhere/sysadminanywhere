package com.sysadminanywhere.inventory.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class InstallationTest {

    @Test
    void constructor_shouldCreateEmptyInstallation() {
        Installation installation = new Installation();

        assertNull(installation.getId());
        assertNull(installation.getComputer());
        assertNull(installation.getSoftware());
        assertNull(installation.getInstallDate());
    }

    @Test
    void setters_shouldUpdateValues() {
        Installation installation = new Installation();
        Computer computer = new Computer();
        computer.setId(1L);
        Software software = new Software();
        software.setId(2L);

        installation.setId(3L);
        installation.setComputer(computer);
        installation.setSoftware(software);
        installation.setInstallDate(LocalDateTime.now());

        assertEquals(3L, installation.getId());
        assertEquals(computer, installation.getComputer());
        assertEquals(software, installation.getSoftware());
        assertNotNull(installation.getInstallDate());
    }
}
