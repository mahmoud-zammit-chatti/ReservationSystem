package com.reservationSys.reservationSys.Exceptions.GeneralExceptions;

public class TooManyRequestsException extends RuntimeException {
    public TooManyRequestsException(String message) {
        super(message);
    }
}
