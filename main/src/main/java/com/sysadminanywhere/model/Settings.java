package com.sysadminanywhere.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Settings {

    private String displayNamePattern = DisplayNamePattern.NONE.name();
    private String loginPattern = LoginPattern.NONE.name();
    private String defaultPassword = "";

}
