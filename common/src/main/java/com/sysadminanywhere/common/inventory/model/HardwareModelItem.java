package com.sysadminanywhere.common.inventory.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HardwareModelItem {

    private Long id;
    private String name;
    private String type;

    List<HardwarePropertyItem> properties;

}