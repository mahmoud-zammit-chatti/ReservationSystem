package com.reservationSys.reservationSys.Exceptions.AuthExceptions;

public class AuthenticationError extends RuntimeException {
    public AuthenticationError(String message) {
        super(message);
    }
}
