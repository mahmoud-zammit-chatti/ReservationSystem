package com.reservationSys.reservationSys.exceptions.AuthExceptions;

public class EmailNotVerifiedException extends RuntimeException{
    public EmailNotVerifiedException(String message) {
        super(message);
    }
}
