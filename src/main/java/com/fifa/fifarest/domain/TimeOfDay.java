package com.fifa.fifarest.domain;

import java.time.LocalTime;

public enum TimeOfDay {
    MORNING(6 * 60, 12 * 60),
    AFTERNOON(12 * 60, 17 * 60),
    EVENING(17 * 60, 21 * 60),
    NIGHT(21 * 60, 24 * 60);

    private final int windowStartMinutes;
    private final int windowEndMinutes;

    TimeOfDay(int windowStartMinutes, int windowEndMinutes) {
        this.windowStartMinutes = windowStartMinutes;
        this.windowEndMinutes = windowEndMinutes;
    }

    public int getWindowStartMinutes() {
        return windowStartMinutes;
    }

    public int getWindowEndMinutes() {
        return windowEndMinutes;
    }

    /** Bulk-generation only ever produces slots inside a single calendar day, so a window's end is capped at 23:59:59 rather than wrapping to 00:00. */
    public LocalTime windowStartTime() {
        return LocalTime.of(windowStartMinutes / 60, windowStartMinutes % 60);
    }

    public static TimeOfDay fromStartTime(LocalTime time) {
        int minutes = time.getHour() * 60 + time.getMinute();
        if (minutes >= NIGHT.windowStartMinutes || minutes < MORNING.windowStartMinutes) {
            return NIGHT;
        }
        if (minutes >= EVENING.windowStartMinutes) {
            return EVENING;
        }
        if (minutes >= AFTERNOON.windowStartMinutes) {
            return AFTERNOON;
        }
        return MORNING;
    }
}
