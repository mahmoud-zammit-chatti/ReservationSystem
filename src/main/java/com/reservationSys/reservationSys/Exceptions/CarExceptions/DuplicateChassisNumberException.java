package com.reservationSys.reservationSys.Exceptions.CarExceptions;

public class DuplicateChassisNumberException extends RuntimeException {
    public DuplicateChassisNumberException(String message) {
        super(message);
    }
}
