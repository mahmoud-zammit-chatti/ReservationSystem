package com.reservationSys.reservationSys.Exceptions.CarExceptions;

public class DuplicatePlateNumberException extends RuntimeException {
    public DuplicatePlateNumberException(String message) {
        super(message);
    }
}
