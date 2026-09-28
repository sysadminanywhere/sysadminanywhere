package com.sysadminanywhere.common.directory.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** Bulk membership change for a group and selected directory objects. */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BulkGroupMembershipDto {
    private List<String> memberDistinguishedNames;
    private String groupDistinguishedName;
    private boolean remove;
}
