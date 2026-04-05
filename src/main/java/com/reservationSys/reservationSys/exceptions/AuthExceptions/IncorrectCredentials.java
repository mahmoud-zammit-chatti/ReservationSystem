package com.reservationSys.reservationSys.exceptions.AuthExceptions;

public class IncorrectCredentials extends RuntimeException {
    public IncorrectCredentials(String message) {
        super(message);
    }
}
