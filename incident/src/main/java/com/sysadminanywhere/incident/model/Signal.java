package com.sysadminanywhere.incident.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Signal {

    private String id;
    private String name;

    private List<Integer> eventIds = new ArrayList<>();
    private List<Integer> correlatedEventIds = new ArrayList<>();
    private List<Integer> orderedSequence = new ArrayList<>();

    private Integer aggregationWindowMinutes = 5;
    private Integer threshold;

    private List<String> groupBy = new ArrayList<>();
    private List<FilterRule> filters = new ArrayList<>();

    private SeverityConfig severity;
    private String recommendationTemplate;

    private boolean meta = false;

}