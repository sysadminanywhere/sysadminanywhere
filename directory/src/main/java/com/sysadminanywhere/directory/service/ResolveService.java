package com.sysadminanywhere.directory.service;

import com.sysadminanywhere.common.directory.model.AD;
import com.sysadminanywhere.common.directory.model.ADSID;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.directory.api.ldap.model.entry.*;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.message.ModifyRequest;
import org.apache.directory.api.ldap.model.message.ModifyRequestImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
public class ResolveService<T> {

    private Class<T> typeArgumentClass;

    public ResolveService(Class<T> typeArgumentClass) {
        this.typeArgumentClass = typeArgumentClass;
    }

    @SneakyThrows
    public Page<T> getADPage(Page<Entry> page) {
        if (page.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), page.getPageable(), page.getTotalElements());
        }

        List<T> content = page.getContent().stream()
                .map(this::getADValue)
                .collect(Collectors.toList());

        return new PageImpl<>(content, page.getPageable(), page.getTotalElements());
    }

    @SneakyThrows
    public Page<T> getADPage(List<Entry> list, Pageable pageable) {
        if (list.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), list.size());

        if (start >= list.size()) {
            return new PageImpl<>(Collections.emptyList(), pageable, list.size());
        }

        List<T> content = list.subList(start, end).stream()
                .map(this::getADValue)
                .collect(Collectors.toList());

        return new PageImpl<>(content, pageable, list.size());
    }

    @SneakyThrows
    public List<T> getADList(List<Entry> list) {
        return list.stream()
                .map(this::getADValue)
                .collect(Collectors.toList());
    }

    @SneakyThrows
    public T getADValue(Entry entry) {

        T result = typeArgumentClass.getDeclaredConstructor().newInstance();

        for (Field field : result.getClass().getDeclaredFields()) {
            AD property = field.getAnnotation(AD.class);
            if (property == null) continue;

            field.setAccessible(true);
            String propertyName = property.name();

            if (propertyName.equalsIgnoreCase("distinguishedname")) {
                field.set(result, entry.getDn().getName());
                continue;
            }

            Value value = entry.get(propertyName) != null ? entry.get(propertyName).get() : null;
            if (value == null) {
                if (field.getType().equals(String.class)) {
                    field.set(result, "");
                }
                continue;
            }

            String fieldType = field.getType().getName();
            switch (fieldType) {
                case "java.lang.String":
                    field.set(result, value.getString());
                    break;
                case "java.time.LocalDateTime":
                    field.set(result, getLocalDateTime(value.getString()));
                    break;
                case "java.util.List":
                    List<String> list = new ArrayList<>();
                    for (Value v : entry.get(property.name())) {
                        list.add(v.getString());
                    }
                    field.set(result, list);
                    break;
                case "int":
                    field.set(result, Integer.parseInt(value.getString()));
                    break;
                case "long":
                    field.set(result, Long.parseLong(value.getString()));
                    break;
                case "boolean":
                    field.set(result, Boolean.parseBoolean(value.getString()));
                    break;
                case "[B": // byte[].class.getName()
                    field.set(result, value.getBytes());
                    break;
                case "java.util.UUID":
                    field.set(result, UUID.nameUUIDFromBytes(value.getBytes()));
                    break;
                case "ADSID":
                    field.set(result, new ADSID(value.getBytes()));
                    break;
                default:
                    break;
            }
        }
        return result;
    }

    @SneakyThrows
    public Entry getEntry(T item) {
        Entry entry = new DefaultEntry();

        for (Field field : item.getClass().getDeclaredFields()) {
            AD property = field.getAnnotation(AD.class);
            if (property == null) continue;

            field.setAccessible(true);
            Object value = field.get(item);
            if (value == null) continue;

            String propertyName = property.name();

            if (propertyName.equalsIgnoreCase("distinguishedname")) {
                entry.setDn(value.toString());
                continue;
            }

            if (value instanceof String || value instanceof Integer || value instanceof Boolean || value instanceof UUID) {
                entry.add(propertyName, value.toString());
            } else if (value instanceof byte[]) {
                entry.add(propertyName, (byte[]) value);
            } else if (value instanceof List<?>) {
                ((List<?>) value).forEach(v -> {
                    try {
                        entry.add(propertyName, v.toString());
                    } catch (LdapException e) {
                        log.error("Error: {}", e.getMessage());
                    }
                });
            }
        }

        return entry;
    }

    public ModifyRequest getModifyRequest(T newEntry, T oldEntry) {
        return getModifyRequest(getEntry(newEntry), getEntry(oldEntry));
    }

    public ModifyRequest getModifyRequest(Entry newEntry, Entry oldEntry) {
        ModifyRequest modifyRequest = new ModifyRequestImpl();
        modifyRequest.setName(newEntry.getDn());

        for (Attribute attribute : newEntry.getAttributes()) {
            if (oldEntry.contains(attribute)) {
                if (!attribute.get().equals(oldEntry.get(attribute.getId()).get())) {
                    if (attribute.get() != null && !attribute.get().equals("")) {
                        Modification modification = new DefaultModification(ModificationOperation.REPLACE_ATTRIBUTE, attribute);
                        modifyRequest.addModification(modification);
                    } else {
                        Modification modification = new DefaultModification(ModificationOperation.REMOVE_ATTRIBUTE, oldEntry.get(attribute.getId()));
                        modifyRequest.addModification(modification);
                    }
                }
            } else {
                Modification modification = new DefaultModification(ModificationOperation.ADD_ATTRIBUTE, attribute);
                modifyRequest.addModification(modification);
            }
        }

        for (Attribute attribute : oldEntry.getAttributes()) {
            if (!newEntry.contains(attribute)) {
                Modification modification = new DefaultModification(ModificationOperation.REMOVE_ATTRIBUTE, attribute);
                modifyRequest.addModification(modification);
            }
        }

        return modifyRequest;
    }

    private LocalDateTime getLocalDateTime(String value) {
        if (value == null || !value.endsWith(".0Z")) {
            return null;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss'.0Z'");
        return LocalDateTime.parse(value, formatter);
    }

}