package com.sysadminanywhere.incident.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SignalTest {

    @Test
    void constructor_shouldCreateEmptySignal() {
        Signal signal = new Signal();

        assertNull(signal.getId());
        assertNull(signal.getName());
        assertNotNull(signal.getEventIds());
        assertTrue(signal.getEventIds().isEmpty());
        assertNotNull(signal.getCorrelatedEventIds());
        assertTrue(signal.getCorrelatedEventIds().isEmpty());
        assertNotNull(signal.getOrderedSequence());
        assertTrue(signal.getOrderedSequence().isEmpty());
        assertEquals(5, signal.getAggregationWindowMinutes());
        assertNull(signal.getThreshold());
        assertNotNull(signal.getGroupBy());
        assertTrue(signal.getGroupBy().isEmpty());
        assertNotNull(signal.getFilters());
        assertTrue(signal.getFilters().isEmpty());
        assertNull(signal.getSeverity());
        assertNull(signal.getRecommendationTemplate());
        assertFalse(signal.isMeta());
    }

    @Test
    void setters_shouldUpdateValues() {
        Signal signal = new Signal();

        signal.setId("SIG-001");
        signal.setName("Test Signal");
        signal.setEventIds(List.of(1001, 1002));
        signal.setCorrelatedEventIds(List.of(1003));
        signal.setOrderedSequence(List.of(1001, 1002));
        signal.setAggregationWindowMinutes(10);
        signal.setThreshold(5);
        signal.setGroupBy(List.of("MachineName"));
        signal.setMeta(true);

        assertEquals("SIG-001", signal.getId());
        assertEquals("Test Signal", signal.getName());
        assertEquals(2, signal.getEventIds().size());
        assertEquals(10, signal.getAggregationWindowMinutes());
        assertEquals(5, signal.getThreshold());
        assertTrue(signal.isMeta());
    }

    @Test
    void equals_shouldCompareSignals() {
        Signal s1 = new Signal();
        s1.setId("SIG-001");
        s1.setName("Signal 1");

        Signal s2 = new Signal();
        s2.setId("SIG-001");
        s2.setName("Signal 1");

        Signal s3 = new Signal();
        s3.setId("SIG-002");
        s3.setName("Signal 2");

        assertEquals(s1, s2);
        assertNotEquals(s1, s3);
    }

    @Test
    void hashCode_shouldBeConsistent() {
        Signal s1 = new Signal();
        s1.setId("SIG-001");

        Signal s2 = new Signal();
        s2.setId("SIG-001");

        assertEquals(s1.hashCode(), s2.hashCode());
    }
}
