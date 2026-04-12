package com.sysadminanywhere.inventory.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ComputerTest {

    @Test
    void constructor_shouldCreateEmptyComputer() {
        Computer computer = new Computer();

        assertNull(computer.getId());
        assertNull(computer.getName());
        assertNull(computer.getCheckingDate());
    }

    @Test
    void setters_shouldUpdateValues() {
        Computer computer = new Computer();

        computer.setId(1L);
        computer.setName("PC001");
        computer.setCheckingDate(LocalDateTime.now());

        assertEquals(1L, computer.getId());
        assertEquals("PC001", computer.getName());
        assertNotNull(computer.getCheckingDate());
    }

    @Test
    void equals_shouldCompareComputers() {
        Computer c1 = new Computer();
        c1.setId(1L);
        c1.setName("PC001");

        Computer c2 = new Computer();
        c2.setId(1L);
        c2.setName("PC001");

        Computer c3 = new Computer();
        c3.setId(2L);
        c3.setName("PC002");

        assertEquals(c1, c2);
        assertNotEquals(c1, c3);
    }

    @Test
    void hashCode_shouldBeConsistent() {
        Computer c1 = new Computer();
        c1.setId(1L);
        c1.setName("PC001");

        Computer c2 = new Computer();
        c2.setId(1L);
        c2.setName("PC001");

        assertEquals(c1.hashCode(), c2.hashCode());
    }

    @Test
    void toString_shouldReturnStringRepresentation() {
        Computer computer = new Computer();
        computer.setName("PC001");

        String str = computer.toString();

        assertNotNull(str);
        assertTrue(str.contains("PC001"));
    }
}
