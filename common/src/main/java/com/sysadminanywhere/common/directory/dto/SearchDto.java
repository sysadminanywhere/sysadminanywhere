package com.sysadminanywhere.common.directory.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchDto {

    private String distinguishedName;
    private String filter;
    private int searchScope;

}
