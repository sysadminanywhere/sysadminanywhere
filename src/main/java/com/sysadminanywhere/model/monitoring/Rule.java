package com.sysadminanywhere.model.monitoring;

import java.util.Map;

public interface Rule {

    String getName();
    String getType();
    String getDescription();
    void execute(Map<String, String>parameters);

}