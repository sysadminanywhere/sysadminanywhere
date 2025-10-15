package com.sysadminanywhere.common.directory.model;

public enum GroupType {
    GLOBAL(0x2),
    DOMAIN_LOCAL(0x4),
    UNIVERSAL(0x8),
    SECURITY(0x80000000);

    private final int value;

    GroupType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static GroupType fromValue(int value) {
        GroupType result = GLOBAL;
        for (GroupType groupType : GroupType.values()) {
            if ((value & groupType.getValue()) == groupType.getValue()) {
                result = groupType;
            }
        }
        return result;
    }
}