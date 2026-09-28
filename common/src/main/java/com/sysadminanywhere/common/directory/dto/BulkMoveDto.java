package com.sysadminanywhere.common.directory.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** Selected object DNs and destination container for a bulk move. */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BulkMoveDto {
    private List<String> distinguishedNames;
    private String targetContainerDistinguishedName;
}
