package com.reservationSys.reservationSys.exceptions.AuthExceptions;

public class AuthenticationError extends RuntimeException {
    public AuthenticationError(String message) {
        super(message);
    }
}
