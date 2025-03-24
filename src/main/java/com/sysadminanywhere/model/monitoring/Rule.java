package com.sysadminanywhere.model.monitoring;

import com.vaadin.flow.component.Component;

import java.util.List;
import java.util.Map;

public interface Rule {

    String getName();
    String getType();
    String getDescription();
    void execute(Map<String, String>parameters);

    List<Component> getControls(Map<String, String>parameters);
    Map<String, String> getParameters();

}