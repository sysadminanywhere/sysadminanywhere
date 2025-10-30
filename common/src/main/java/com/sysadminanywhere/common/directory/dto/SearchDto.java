package com.sysadminanywhere.common.directory.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SearchDto {

    private String distinguishedName;
    private String filter;
    private int searchScope;
    private String[] attributes;

    public SearchDto(String distinguishedName, String filter, int searchScope, String... attributes) {
        this.distinguishedName = distinguishedName;
        this.filter = filter;
        this.searchScope = searchScope;
        this.attributes = attributes;
    }

}