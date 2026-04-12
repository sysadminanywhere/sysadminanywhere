package com.sysadminanywhere.service;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UtilsTest {

    @Test
    void formatDuration_shouldFormatMilliseconds() {
        Duration duration = Duration.ofMillis(500);

        String result = Utils.formatDuration(duration, true);

        assertEquals("500 ms", result);
    }

    @Test
    void formatDuration_shouldFormatSeconds() {
        Duration duration = Duration.ofSeconds(30);

        String result = Utils.formatDuration(duration, false);

        assertEquals("30 seconds", result);
    }

    @Test
    void formatDuration_shouldFormatSecondsWithMillis() {
        Duration duration = Duration.ofMillis(30500);

        String result = Utils.formatDuration(duration, true);

        assertEquals("30 seconds 500 ms", result);
    }

    @Test
    void formatDuration_shouldFormatMinutes() {
        Duration duration = Duration.ofMinutes(5);

        String result = Utils.formatDuration(duration, false);

        assertEquals("5 minutes", result);
    }

    @Test
    void formatDuration_shouldFormatMinutesWithSeconds() {
        Duration duration = Duration.ofMinutes(5).plusSeconds(30);

        String result = Utils.formatDuration(duration, true);

        assertEquals("5 minutes 30 seconds", result);
    }

    @Test
    void formatDuration_shouldFormatHours() {
        Duration duration = Duration.ofHours(2);

        String result = Utils.formatDuration(duration, false);

        assertEquals("2 hours", result);
    }

    @Test
    void formatDuration_shouldFormatHoursWithMinutes() {
        Duration duration = Duration.ofHours(2).plusMinutes(45);

        String result = Utils.formatDuration(duration, true);

        assertEquals("2 hours 45 minutes", result);
    }

    @Test
    void formatDuration_shouldFormatDays() {
        Duration duration = Duration.ofDays(3);

        String result = Utils.formatDuration(duration, false);

        assertEquals("3 days", result);
    }

    @Test
    void formatDuration_shouldFormatDaysWithHours() {
        Duration duration = Duration.ofDays(3).plusHours(12);

        String result = Utils.formatDuration(duration, true);

        assertEquals("3 days 12 hours", result);
    }

    @Test
    void formatDuration_shouldHandleNegativeDuration() {
        Duration duration = Duration.ofSeconds(-30);

        String result = Utils.formatDuration(duration, false);

        assertEquals("-30 seconds", result);
    }

    @Test
    void formatDuration_shouldHandleZeroDuration() {
        Duration duration = Duration.ZERO;

        String result = Utils.formatDuration(duration, false);

        assertEquals("0 ms", result);
    }

    @Test
    void formatInstant_shouldFormatCorrectly() {
        Instant instant = Instant.parse("2024-01-15T10:30:45Z");

        String result = Utils.formatInstant(instant);

        assertNotNull(result);
        assertTrue(result.matches("\\d{2}\\.\\d{2}\\.\\d{4} \\d{2}:\\d{2}:\\d{2}"));
    }

    @Test
    void formatLocalDateTime_shouldFormatCorrectly() {
        LocalDateTime dateTime = LocalDateTime.of(2024, 1, 15, 10, 30, 45);

        String result = Utils.formatLocalDateTime(dateTime);

        assertEquals("15.01.2024 10:30:45", result);
    }

    @Test
    void formatDuration_shouldHandleLargeDuration() {
        Duration duration = Duration.ofDays(365);

        String result = Utils.formatDuration(duration, false);

        assertEquals("365 days", result);
    }
}
