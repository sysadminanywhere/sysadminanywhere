package com.sysadminanywhere.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReportItem {

    private String id;
    private String name;
    private String description;
    private String filter;
    private String[] columns;

}
