package com.sysadminanywhere.model;

public enum LoginPattern {

    FLAST("FLast (e.g. hsimpson)","(?<FirstName>\\S)\\S+ (?<LastName>\\S+)","${FirstName}${LastName}"),
    F_LAST("F.Last (e.g. h.simpson)","(?<FirstName>\\S)\\S+ (?<LastName>\\S+)","${FirstName}.${LastName}"),
    FIRST_LAST("First.Last (e.g. homer.simpson)","(?<FirstName>\\S) (?<LastName>\\S+)","${FirstName}.${LastName}"),
    LAST("Last (e.g. simpson)","(?<FirstName>\\S) (?<LastName>\\S+)","${LastName}"),
    LASTF("LastF (e.g. simpsonh)","(?<FirstName>\\S)\\S+ (?<LastName>\\S+)","${LastName}${FirstName}"),
    NONE("None","","");

    private String title;
    private String pattern;
    private String format;

    LoginPattern(String title, String pattern, String format) {
        this.title = title;
        this.pattern = pattern;
        this.format = format;
    }

    public String getTitle() {
        return title;
    }

    public String getPattern() {
        return pattern;
    }

    public String getFormat() {
        return format;
    }

}