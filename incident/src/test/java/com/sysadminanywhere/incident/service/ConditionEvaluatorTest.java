package com.sysadminanywhere.incident.service;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ConditionEvaluatorTest {

    @Test
    void evaluate_shouldReturnTrueForSimpleCondition() {
        boolean result = ConditionEvaluator.evaluate("true", Map.of());

        assertTrue(result);
    }

    @Test
    void evaluate_shouldReturnFalseForSimpleCondition() {
        boolean result = ConditionEvaluator.evaluate("false", Map.of());

        assertFalse(result);
    }

    @Test
    void evaluate_shouldEvaluateVariableExpressions() {
        Map<String, Object> context = Map.of("count", 10);

        boolean result = ConditionEvaluator.evaluate("#count > 5", context);

        assertTrue(result);
    }

    @Test
    void evaluate_shouldHandleStringComparisons() {
        Map<String, Object> context = Map.of("status", "error");

        boolean result = ConditionEvaluator.evaluate("#status == 'error'", context);

        assertTrue(result);
    }

    @Test
    void evaluate_shouldHandleComplexExpressions() {
        Map<String, Object> context = Map.of(
            "count", 10,
            "status", "active"
        );

        boolean result = ConditionEvaluator.evaluate("#count > 5 && #status == 'active'", context);

        assertTrue(result);
    }

    @Test
    void evaluate_shouldReturnFalseWhenConditionNotMet() {
        Map<String, Object> context = Map.of("count", 3);

        boolean result = ConditionEvaluator.evaluate("#count > 5", context);

        assertFalse(result);
    }

    @Test
    void evaluate_shouldHandleNumericComparisons() {
        Map<String, Object> context = Map.of("threshold", 100);

        assertTrue(ConditionEvaluator.evaluate("#threshold >= 100", context));
        assertFalse(ConditionEvaluator.evaluate("#threshold < 100", context));
    }

    @Test
    void evaluate_shouldHandleBooleanVariables() {
        Map<String, Object> context = Map.of("enabled", true);

        assertTrue(ConditionEvaluator.evaluate("#enabled == true", context));
        assertTrue(ConditionEvaluator.evaluate("#enabled", context));
    }
}
