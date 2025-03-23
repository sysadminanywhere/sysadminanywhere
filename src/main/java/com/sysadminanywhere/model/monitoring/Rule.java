package com.sysadminanywhere.model.monitoring;

import java.util.Map;

public interface Rule {

    String getName();
    Map<String, String> getParameters();
    void execute(Map<String, String>parameters);

}