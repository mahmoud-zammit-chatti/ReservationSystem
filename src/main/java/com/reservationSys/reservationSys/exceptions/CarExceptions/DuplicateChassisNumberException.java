package com.reservationSys.reservationSys.exceptions.CarExceptions;

public class DuplicateChassisNumberException extends RuntimeException {
    public DuplicateChassisNumberException(String message) {
        super(message);
    }
}
