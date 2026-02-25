package com.reservationSys.reservationSys.Domain.reservation;

public enum Duration {
    SIX_HOURS(6),
    EIGHT_HOURS(8),
    TWELVE_HOURS(12);

    private final int hours;

    Duration(int hours) {
        this.hours = hours;
    }

    public int getHours() {
        return hours;
    }
}
