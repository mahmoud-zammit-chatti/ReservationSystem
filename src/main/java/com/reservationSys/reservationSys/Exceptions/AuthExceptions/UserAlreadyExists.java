package com.reservationSys.reservationSys.Exceptions.AuthExceptions;

public class UserAlreadyExists extends RuntimeException {
    public UserAlreadyExists(String message) {
        super(message);
    }
}
