package com.sysadminanywhere.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ComputerDto {

    private Long id;
    private String name;
    private String dns;

}