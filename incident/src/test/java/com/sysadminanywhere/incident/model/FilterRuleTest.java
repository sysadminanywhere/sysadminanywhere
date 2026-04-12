package com.sysadminanywhere.incident.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FilterRuleTest {

    @Test
    void constructor_shouldCreateEmptyFilterRule() {
        FilterRule rule = new FilterRule();

        assertNull(rule.getField());
        assertNull(rule.getValue());
    }

    @Test
    void setters_shouldUpdateValues() {
        FilterRule rule = new FilterRule();

        rule.setField("UserName");
        rule.setValue("admin");

        assertEquals("UserName", rule.getField());
        assertEquals("admin", rule.getValue());
    }

    @Test
    void equals_shouldCompareFilterRules() {
        FilterRule r1 = new FilterRule();
        r1.setField("UserName");
        r1.setValue("admin");

        FilterRule r2 = new FilterRule();
        r2.setField("UserName");
        r2.setValue("admin");

        FilterRule r3 = new FilterRule();
        r3.setField("MachineName");
        r3.setValue("PC001");

        assertEquals(r1, r2);
        assertNotEquals(r1, r3);
    }

    @Test
    void hashCode_shouldBeConsistent() {
        FilterRule r1 = new FilterRule();
        r1.setField("UserName");
        r1.setValue("admin");

        FilterRule r2 = new FilterRule();
        r2.setField("UserName");
        r2.setValue("admin");

        assertEquals(r1.hashCode(), r2.hashCode());
    }
}
