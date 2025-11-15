package com.sysadminanywhere.domain;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ObjectToListMapConverter {

    public static List<Map<String, Object>> convertToListMap(List<?> objectList) {
        List<Map<String, Object>> result = new ArrayList<>();

        if (objectList == null || objectList.isEmpty()) {
            return result;
        }

        for (Object obj : objectList) {
            if (obj != null) {
                Map<String, Object> fieldMap = convertObjectToMap(obj);
                result.add(fieldMap);
            }
        }

        return result;
    }

    private static Map<String, Object> convertObjectToMap(Object obj) {
        Map<String, Object> fieldMap = new HashMap<>();

        try {
            Class<?> clazz = obj.getClass();
            Field[] fields = clazz.getDeclaredFields();

            for (Field field : fields) {
                field.setAccessible(true);
                Object value = field.get(obj);
                fieldMap.put(field.getName(), value);
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Error", e);
        }

        return fieldMap;
    }

}