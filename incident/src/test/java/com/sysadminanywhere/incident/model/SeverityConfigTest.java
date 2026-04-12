package com.sysadminanywhere.incident.model;

import com.sysadminanywhere.common.incident.model.Severity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SeverityConfigTest {

    @Test
    void constructor_shouldCreateEmptySeverityConfig() {
        SeverityConfig config = new SeverityConfig();

        assertNull(config.getDefaultSeverity());
    }

    @Test
    void setters_shouldUpdateValues() {
        SeverityConfig config = new SeverityConfig();

        config.setDefaultSeverity(Severity.HIGH);

        assertEquals(Severity.HIGH, config.getDefaultSeverity());
    }

    @Test
    void equals_shouldCompareSeverityConfigs() {
        SeverityConfig c1 = new SeverityConfig();
        c1.setDefaultSeverity(Severity.HIGH);

        SeverityConfig c2 = new SeverityConfig();
        c2.setDefaultSeverity(Severity.HIGH);

        SeverityConfig c3 = new SeverityConfig();
        c3.setDefaultSeverity(Severity.LOW);

        assertEquals(c1, c2);
        assertNotEquals(c1, c3);
    }

    @Test
    void hashCode_shouldBeConsistent() {
        SeverityConfig c1 = new SeverityConfig();
        c1.setDefaultSeverity(Severity.MEDIUM);

        SeverityConfig c2 = new SeverityConfig();
        c2.setDefaultSeverity(Severity.MEDIUM);

        assertEquals(c1.hashCode(), c2.hashCode());
    }
}
