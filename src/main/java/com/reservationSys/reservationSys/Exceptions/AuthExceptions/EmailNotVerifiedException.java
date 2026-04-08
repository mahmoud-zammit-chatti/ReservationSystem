package com.reservationSys.reservationSys.Exceptions.AuthExceptions;

public class EmailNotVerifiedException extends RuntimeException{
    public EmailNotVerifiedException(String message) {
        super(message);
    }
}
