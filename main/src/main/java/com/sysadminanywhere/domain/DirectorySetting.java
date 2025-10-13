package com.sysadminanywhere.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class DirectorySetting {

    private List<String> groupsAllowed;

}
