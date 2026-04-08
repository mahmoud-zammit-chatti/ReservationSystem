package com.reservationSys.reservationSys.Exceptions.GeneralExceptions;

public class NotAuthorizedException extends RuntimeException {
    public NotAuthorizedException(String message) {
        super(message);
    }
}
