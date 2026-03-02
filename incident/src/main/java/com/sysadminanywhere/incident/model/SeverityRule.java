package com.sysadminanywhere.incident.model;

import lombok.Data;

@Data
public class SeverityRule {

    /**
     * Example:
     * user_name IN ('Domain Admins','Enterprise Admins')
     */
    private String condition;

    private Severity severity;

}