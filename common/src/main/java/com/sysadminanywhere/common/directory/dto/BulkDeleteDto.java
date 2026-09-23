package com.sysadminanywhere.common.directory.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** Distinguished names selected for a single bulk delete request. */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BulkDeleteDto {
    private List<String> distinguishedNames;
}
