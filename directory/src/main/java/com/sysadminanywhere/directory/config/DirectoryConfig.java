package com.sysadminanywhere.directory.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class DirectoryConfig {

    private List<String> groupsAllowed;

}
