package com.reservationSys.reservationSys.exceptions.GeneralExceptions;

public class NotAuthorizedException extends RuntimeException {
    public NotAuthorizedException(String message) {
        super(message);
    }
}
