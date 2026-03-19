package com.reservationSys.reservationSys.exceptions.CarExceptions;


import lombok.Data;
import lombok.EqualsAndHashCode;


@EqualsAndHashCode(callSuper = true)
@Data
public class BlockedCarException extends RuntimeException {

    private final long hoursRemaining;
    private final long minutesRemaining;

    public BlockedCarException(String message, long hoursRemaining, long minutesRemaining) {
        super(message);
        this.hoursRemaining = hoursRemaining;
        this.minutesRemaining = minutesRemaining;
    }
}
