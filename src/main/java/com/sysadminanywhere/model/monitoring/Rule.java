package com.sysadminanywhere.model.monitoring;

import com.vaadin.flow.component.Component;

import java.util.List;
import java.util.Map;

public interface Rule {

    String getName();
    String getType();
    String getDescription();
    String execute(Map<String, String>parameters);

    String getDefaultCron();

    List<Component> getControls(Map<String, String>parameters);
    Map<String, String> getParameters();

}