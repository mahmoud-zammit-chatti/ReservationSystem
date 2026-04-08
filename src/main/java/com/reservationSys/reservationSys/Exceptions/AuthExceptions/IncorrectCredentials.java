package com.reservationSys.reservationSys.Exceptions.AuthExceptions;

public class IncorrectCredentials extends RuntimeException {
    public IncorrectCredentials(String message) {
        super(message);
    }
}
