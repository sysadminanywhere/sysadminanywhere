package com.sysadminanywhere.common.inventory.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ComputerItem {

    private Long id;
    private String name;
    private String dns;

}
