package com.sysadminanywhere.domain;

import com.sysadminanywhere.common.directory.model.AD;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ObjectToListMapConverter {

    public static List<Map<String, Object>> convertToListMap(List<?> objectList, String[] attributes) {
        List<Map<String, Object>> result = new ArrayList<>();

        if (objectList == null || objectList.isEmpty()) {
            return result;
        }

        for (Object obj : objectList) {
            if (obj != null) {
                Map<String, Object> fieldMap = convertObjectToMap(obj, attributes);
                result.add(fieldMap);
            }
        }

        return result;
    }

    private static Map<String, Object> convertObjectToMap(Object obj, String[] attributes) {
        Map<String, Object> fieldMap = new HashMap<>();

        try {
            Class<?> clazz = obj.getClass();
            Field[] fields = clazz.getDeclaredFields();

            for (Field field : fields) {
                field.setAccessible(true);
                Object value = field.get(obj);
                AD property = field.getAnnotation(AD.class);
                if(Arrays.asList(attributes).contains(property.name())) {
                    if(value instanceof LocalDateTime) {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                        value = ((LocalDateTime) value).format(formatter);
                    }
                    fieldMap.put(property.name(), value);
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Error", e);
        }

        return fieldMap;
    }

}