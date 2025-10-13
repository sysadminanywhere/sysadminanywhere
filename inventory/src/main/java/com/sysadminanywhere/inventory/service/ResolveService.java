package com.sysadminanywhere.inventory.service;

import com.sysadminanywhere.inventory.model.AD;
import com.sysadminanywhere.inventory.model.ADSID;
import lombok.SneakyThrows;
import org.apache.directory.api.ldap.model.entry.*;
import org.apache.directory.api.ldap.model.message.ModifyRequest;
import org.apache.directory.api.ldap.model.message.ModifyRequestImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ResolveService<T> {

    private Class<T> typeArgumentClass;

    public ResolveService(Class<T> typeArgumentClass) {
        this.typeArgumentClass = typeArgumentClass;
    }

    @SneakyThrows
    public Page<T> getADPage(List<Entry> list, Pageable pageable) {
        List<T> content = new ArrayList<>();

        if (!list.isEmpty()) {

            int start = pageable.getPageNumber() * pageable.getPageSize();
            int end = start + pageable.getPageSize();

            if (end > list.size())
                end = list.size();

            for (int i = start; i < end; i++) {
                content.add(getADValue(list.get(i)));
            }

        }

        return new PageImpl<>(content, pageable, list.size());
    }

    @SneakyThrows
    public List<T> getADList(List<Entry> list) {
        List<T> content = new ArrayList<>();

        if (!list.isEmpty()) {
            for (Entry entry : list)
                content.add(getADValue(entry));
        }

        return content;
    }

    @SneakyThrows
    public T getADValue(Entry entry) {

        T result = typeArgumentClass.newInstance();

        Field[] fields = result.getClass().getDeclaredFields();

        for (Field field : fields) {
            AD property = field.getAnnotation(AD.class);
            if (property != null) {
                field.setAccessible(true);
                if (property.name().equalsIgnoreCase("distinguishedname")) {
                    field.set(result, entry.getDn().getName());
                } else {
                    if (entry.get(property.name()) != null) {
                        Value value = entry.get(property.name()).get();

                        if (field.getType().getName().equalsIgnoreCase(String.class.getName())) {
                            if(value == null)
                                field.set(result, "");
                            else
                                field.set(result, value.getString());
                        }

                        if (field.getType().getName().equalsIgnoreCase(LocalDateTime.class.getName())) {
                            field.set(result, getLocalDateTime(value.getString()));
                        }

                        if (field.getType().getName().equalsIgnoreCase("java.util.List")) {
                            List<String> list = new ArrayList<>();
                            for (Value v : entry.get(property.name())) {
                                list.add(v.getString());
                            }
                            field.set(result, list);
                        }

                        if (field.getType().getName().equalsIgnoreCase(int.class.getName())) {
                            field.set(result, Integer.valueOf(value.getString()));
                        }

                        if (field.getType().getName().equalsIgnoreCase(long.class.getName())) {
                            field.set(result, Integer.valueOf(value.getString()));
                        }

                        if (field.getType().getName().equalsIgnoreCase(boolean.class.getName())) {
                            field.set(result, Boolean.valueOf(value.getString()));
                        }

                        if (field.getType().getName().equalsIgnoreCase(byte[].class.getName())) {
                            field.set(result, value.getBytes());
                        }

                        if (field.getType().getName().equalsIgnoreCase(UUID.class.getName())) {
                            field.set(result, UUID.nameUUIDFromBytes(value.getBytes()));
                        }

                        if (field.getType().getName().equalsIgnoreCase(ADSID.class.getName())) {
                            field.set(result, new ADSID(value.getBytes()));
                        }

                    } else {
                        // Set default value for String - ""
                        if (field.getType().getName().equalsIgnoreCase(String.class.getName())) {
                            field.set(result, "");
                        }
                    }
                }
            }
        }

        return result;
    }

    @SneakyThrows
    public Entry getEntry(T item) {
        Entry entry = new DefaultEntry();

        Field[] fields = item.getClass().getDeclaredFields();

        for (Field field : fields) {
            AD property = field.getAnnotation(AD.class);
            if (property != null) {
                field.setAccessible(true);
                if (property.name().equalsIgnoreCase("distinguishedname")) {
                    entry.setDn(field.get(item).toString());
                } else {

                    if (field.get(item) != null) {

                        if (field.getType().getName().equalsIgnoreCase(String.class.getName())) {
                            entry.add(property.name(), field.get(item).toString());
                        }

                        if (field.getType().getName().equalsIgnoreCase(int.class.getName())) {
                            entry.add(property.name(), field.get(item).toString());
                        }

                        if (field.getType().getName().equalsIgnoreCase(boolean.class.getName())) {
                            entry.add(property.name(), field.get(item).toString());
                        }

                        if (field.getType().getName().equalsIgnoreCase(byte[].class.getName())) {
                            entry.add(property.name(), (byte[]) field.get(item));
                        }

                        if (field.getType().getName().equalsIgnoreCase(UUID.class.getName())) {
                            entry.add(property.name(), field.get(item).toString());
                        }

                        if (field.getType().getName().equalsIgnoreCase("java.util.List")) {
                            for (String s : (List<String>) field.get(item)) {
                                entry.add(property.name(), s);
                            }
                        }

                    }
                }
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
        try {
            if (value.endsWith(".0Z")) {
                int year = Integer.valueOf(value.substring(0, 4));
                int month = Integer.valueOf(value.substring(4, 6));
                int day = Integer.valueOf(value.substring(6, 8));

                int hour = 0;
                int minute = 0;
                int second = 0;

                if (value.length() > 8) {
                    hour = Integer.valueOf(value.substring(8, 10));
                    minute = Integer.valueOf(value.substring(10, 12));
                    second = Integer.valueOf(value.substring(12, 14));
                }
                return LocalDateTime.of(year, month, day, hour, minute, second);
            } else {
                return LocalDateTime.parse(value);
            }
        } catch (Exception e) {
            return null;
        }
    }

}