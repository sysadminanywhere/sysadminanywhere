package com.sysadminanywhere.model.monitoring;

import java.util.Map;

public interface Rule {

    void execute(Map<String, Object>parameters);

}