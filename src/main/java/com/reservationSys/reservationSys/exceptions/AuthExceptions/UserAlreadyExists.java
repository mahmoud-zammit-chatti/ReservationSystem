package com.reservationSys.reservationSys.exceptions.AuthExceptions;

public class UserAlreadyExists extends RuntimeException {
    public UserAlreadyExists(String message) {
        super(message);
    }
}
