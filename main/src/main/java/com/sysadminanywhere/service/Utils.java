package com.sysadminanywhere.service;

import java.time.Duration;

public class Utils {

    public static String formatDuration(Duration duration, boolean showMillis) {
        long millis = duration.toMillis();
        long absMillis = Math.abs(millis);

        if (absMillis < 1000) {
            return millis + " ms";
        } else if (absMillis < 60000) {
            long seconds = millis / 1000;
            long ms = millis % 1000;
            if (showMillis && ms > 0) {
                return String.format("%d seconds %d ms", seconds, Math.abs(ms));
            } else {
                return seconds + " seconds";
            }
        } else if (absMillis < 3600000) {
            long minutes = duration.toMinutes();
            long seconds = duration.toSeconds() % 60;
            if (showMillis && seconds > 0) {
                return String.format("%d minutes %d seconds", minutes, seconds);
            } else {
                return minutes + " minutes";
            }
        } else if (absMillis < 86400000) {
            long hours = duration.toHours();
            long minutes = duration.toMinutes() % 60;
            if (showMillis && minutes > 0) {
                return String.format("%d hours %d minutes", hours, minutes);
            } else {
                return hours + " hours";
            }
        } else {
            long days = duration.toDays();
            long hours = duration.toHours() % 24;
            if (showMillis && hours > 0) {
                return String.format("%d days %d hours", days, hours);
            } else {
                return days + " days";
            }
        }
    }

}
