package com.sysadminanywhere.service;

import com.sysadminanywhere.model.WMIAttribute;
import lombok.SneakyThrows;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WmiResolveService<T> {

    private Class<T> typeArgumentClass;

    public WmiResolveService(Class<T> typeArgumentClass) {
        this.typeArgumentClass = typeArgumentClass;
    }

    @SneakyThrows
    public Page<T> GetValues(List<Map<String, Object>> list, Pageable pageable) {
        List<T> content = new ArrayList<>();

        if (!list.isEmpty()) {

            int start = pageable.getPageNumber() * pageable.getPageSize();
            int end = start + pageable.getPageSize();

            if (end > list.size())
                end = list.size();

            for (int i = start; i < end; i++) {
                content.add(getValue(list.get(i)));
            }

        }

        return new PageImpl<>(content, pageable, list.size());
    }

    @SneakyThrows
    public List<T> GetValues(List<Map<String, Object>> list) {
        List<T> content = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {
            content.add(getValue(list.get(i)));
        }

        return content;
    }

    @SneakyThrows
    public T getValue(Map<String, Object> item) {
        T result = typeArgumentClass.newInstance();

        Field[] fields = result.getClass().getDeclaredFields();

        for (Field field : fields) {
            WMIAttribute property = field.getAnnotation(WMIAttribute.class);
            if (property != null) {
                field.setAccessible(true);

                if (item.containsKey(property.name())) {
                    var value = item.get(property.name());
                    if ((value != null)) {
                        field.set(result, item.get(property.name()).toString());
                    }
                }
            }
        }

        return result;
    }

}