package com.sysadminanywhere.inventory.mapper;

import com.sysadminanywhere.common.inventory.model.SoftwareItem;
import com.sysadminanywhere.inventory.entity.Software;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SoftwareMapper {

    public SoftwareItem toDto(Software software) {
        SoftwareItem dto = new SoftwareItem();

        dto.setId(software.getId());
        dto.setName(software.getName());
        dto.setVendor(software.getVendor());
        dto.setVersion(software.getVersion());

        return dto;
    }

    public List<SoftwareItem> toDtoList(List<Software> softwareList) {
        List<SoftwareItem> list = new ArrayList<>();

        for (Software software : softwareList) {
            list.add(toDto(software));
        }

        return list;
    }

}
