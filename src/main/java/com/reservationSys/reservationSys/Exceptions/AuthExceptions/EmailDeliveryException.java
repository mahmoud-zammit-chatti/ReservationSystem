package com.reservationSys.reservationSys.Exceptions.AuthExceptions;

public class EmailDeliveryException extends RuntimeException {
    public EmailDeliveryException(String message) {
        super(message);
    }
}
