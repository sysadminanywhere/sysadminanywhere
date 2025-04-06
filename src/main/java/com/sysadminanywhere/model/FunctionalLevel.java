package com.sysadminanywhere.model;

public enum FunctionalLevel {
    WINDOWS_2000(0, "Windows 2000"),
    WINDOWS_2003(1, "Windows Server 2003"),
    WINDOWS_2008(2, "Windows Server 2008"),
    WINDOWS_2008_R2(3, "Windows Server 2008 R2"),
    WINDOWS_2012(4, "Windows Server 2012"),
    WINDOWS_2012_R2(5, "Windows Server 2012 R2"),
    WINDOWS_2016(6, "Windows Server 2016"),
    WINDOWS_2019(7, "Windows Server 2019"),
    WINDOWS_2022(8, "Windows Server 2022 (возможно зарезервирован)"),
    UNKNOWN(-1, "Неизвестный уровень");

    private final int level;
    private final String description;

    FunctionalLevel(int level, String description) {
        this.level = level;
        this.description = description;
    }

    public int getLevel() {
        return level;
    }

    public String getDescription() {
        return description;
    }

    public static FunctionalLevel fromValue(int value) {
        for (FunctionalLevel fl : values()) {
            if (fl.level == value) {
                return fl;
            }
        }
        return UNKNOWN;
    }

    public static String fromValue(String value) {
        return fromValue(Integer.parseInt(value)).description;
    }

}