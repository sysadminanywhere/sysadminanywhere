package com.sysadminanywhere.inventory.mapper;

import com.sysadminanywhere.inventory.controller.dto.SoftwareDto;
import com.sysadminanywhere.inventory.entity.Software;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SoftwareMapper {

    public SoftwareDto toDto(Software software) {
        SoftwareDto dto = new SoftwareDto();

        dto.setId(software.getId());
        dto.setName(software.getName());
        dto.setVendor(software.getVendor());
        dto.setVersion(software.getVersion());

        return dto;
    }

    public List<SoftwareDto> toDtoList(List<Software> softwareList) {
        List<SoftwareDto> list = new ArrayList<>();

        for (Software software : softwareList) {
            list.add(toDto(software));
        }

        return list;
    }

}
