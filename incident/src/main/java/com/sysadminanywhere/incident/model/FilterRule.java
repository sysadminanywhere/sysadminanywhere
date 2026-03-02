package com.sysadminanywhere.incident.model;

import lombok.Data;

@Data
public class FilterRule {

    private String field;

    /**
     * =, !=, IN, NOT_IN, LIKE, >, <, >=, <=
     */
    private Operator operator;

    /**
     * Single value OR comma separated list for IN
     */
    private String value;

}