package com.reservationSys.reservationSys.exceptions.CarExceptions;

public class DuplicatePlateNumberException extends RuntimeException {
    public DuplicatePlateNumberException(String message) {
        super(message);
    }
}
