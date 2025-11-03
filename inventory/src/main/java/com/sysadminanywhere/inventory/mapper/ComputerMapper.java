package com.sysadminanywhere.inventory.mapper;

import com.sysadminanywhere.common.inventory.model.ComputerItem;
import com.sysadminanywhere.inventory.entity.Computer;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ComputerMapper {

    public ComputerItem toDto(Computer computer) {
        ComputerItem dto = new ComputerItem();

        dto.setId(computer.getId());
        dto.setName(computer.getName());

        return dto;
    }

    public List<ComputerItem> toDtoList(List<Computer> computers) {
        List<ComputerItem> list = new ArrayList<>();

        for (Computer computer : computers) {
            list.add(toDto(computer));
        }

        return list;
    }

}