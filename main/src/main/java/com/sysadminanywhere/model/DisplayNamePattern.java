package com.sysadminanywhere.model;

public enum DisplayNamePattern {

    FIRST_LAST("First Last (e.g. Homer Simpson)", "(?<FirstName>\\S+) (?<LastName>\\S+)"),
    LAST_FIRST("Last First (e.g. Simpson Homer)", "(?<LastName>\\S+) (?<FirstName>\\S+)"),
    FIRST_MIDDLE_LAST("First Middle Last (e.g. Homer Jay Simpson)", "(?<FirstName>\\S+) (?<Middle>\\S+) (?<LastName>\\S+)"),
    LAST_FIRST_MIDDLE("Last First Middle (e.g. Simpson Homer Jay)", "(?<LastName>\\S+) (?<FirstName>\\S+) (?<Middle>\\S+)"),
    NONE("None","");

    private String title;
    private String pattern;

    DisplayNamePattern(String title, String pattern) {
        this.title = title;
        this.pattern = pattern;
    }

    public String getTitle() {
        return title;
    }

    public String getPattern() {
        return pattern;
    }

}