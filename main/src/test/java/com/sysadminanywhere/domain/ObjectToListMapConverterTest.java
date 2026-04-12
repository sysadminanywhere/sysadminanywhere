package com.sysadminanywhere.domain;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ObjectToListMapConverterTest {

    @Test
    void convertToListMap_shouldHandleNullList() {
        List<Map<String, Object>> result = ObjectToListMapConverter.convertToListMap(
            null, 
            new String[]{"cn"}
        );

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void convertToListMap_shouldHandleEmptyList() {
        List<Map<String, Object>> result = ObjectToListMapConverter.convertToListMap(
            List.of(), 
            new String[]{"cn"}
        );

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
