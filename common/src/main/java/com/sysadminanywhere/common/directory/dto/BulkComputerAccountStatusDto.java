package com.sysadminanywhere.common.directory.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** Account state change for selected computer accounts. */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BulkComputerAccountStatusDto {
    private List<String> distinguishedNames;
    private boolean accountDisabled;
}
