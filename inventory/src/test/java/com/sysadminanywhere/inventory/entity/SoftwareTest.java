package com.sysadminanywhere.inventory.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SoftwareTest {

    @Test
    void constructor_shouldCreateEmptySoftware() {
        Software software = new Software();

        assertNull(software.getId());
        assertNull(software.getName());
        assertNull(software.getVersion());
        assertNull(software.getVendor());
    }

    @Test
    void setters_shouldUpdateValues() {
        Software software = new Software();

        software.setId(1L);
        software.setName("Microsoft Office");
        software.setVersion("2021");
        software.setVendor("Microsoft");

        assertEquals(1L, software.getId());
        assertEquals("Microsoft Office", software.getName());
        assertEquals("2021", software.getVersion());
        assertEquals("Microsoft", software.getVendor());
    }

    @Test
    void equals_shouldCompareSoftware() {
        Software s1 = new Software();
        s1.setId(1L);
        s1.setName("Office");
        s1.setVendor("Microsoft");

        Software s2 = new Software();
        s2.setId(1L);
        s2.setName("Office");
        s2.setVendor("Microsoft");

        Software s3 = new Software();
        s3.setId(2L);
        s3.setName("Photoshop");
        s3.setVendor("Adobe");

        assertEquals(s1, s2);
        assertNotEquals(s1, s3);
    }

    @Test
    void hashCode_shouldBeConsistent() {
        Software s1 = new Software();
        s1.setName("Test Software");
        s1.setVendor("Test Vendor");

        Software s2 = new Software();
        s2.setName("Test Software");
        s2.setVendor("Test Vendor");

        assertEquals(s1.hashCode(), s2.hashCode());
    }
}
