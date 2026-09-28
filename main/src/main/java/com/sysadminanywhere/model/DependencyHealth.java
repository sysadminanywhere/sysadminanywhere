package com.sysadminanywhere.model;

import java.util.Map;

public record DependencyHealth(Map<String, String> services) { }
