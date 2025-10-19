package com.sysadminanywhere.common.directory.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Container {

    private String name;
    private String distinguishedName;
    private Container parent;

}
