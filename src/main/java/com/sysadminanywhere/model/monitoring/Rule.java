package com.sysadminanywhere.model.monitoring;

import java.util.Map;

public interface Rule {

    void setParameters(Map<String, Object>parameters);
    String getName();
    void execute();
    String getCronExpression();

}