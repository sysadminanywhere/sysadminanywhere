package com.sysadminanywhere.inventory.mapper;

import com.sysadminanywhere.inventory.controller.dto.ComputerDto;
import com.sysadminanywhere.inventory.entity.Computer;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ComputerMapper {

    public ComputerDto toDto(Computer computer) {
        ComputerDto dto = new ComputerDto();

        dto.setId(computer.getId());
        dto.setName(computer.getName());
        dto.setDns(computer.getDns());

        return dto;
    }

    public List<ComputerDto> toDtoList(List<Computer> computers) {
        List<ComputerDto> list = new ArrayList<>();

        for (Computer computer : computers) {
            list.add(toDto(computer));
        }

        return list;
    }

}