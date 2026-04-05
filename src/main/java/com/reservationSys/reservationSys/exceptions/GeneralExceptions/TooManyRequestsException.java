package com.reservationSys.reservationSys.exceptions.GeneralExceptions;

public class TooManyRequestsException extends RuntimeException {
    public TooManyRequestsException(String message) {
        super(message);
    }
}
