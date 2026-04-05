package com.reservationSys.reservationSys.exceptions.AuthExceptions;

public class EmailDeliveryException extends RuntimeException {
    public EmailDeliveryException(String message) {
        super(message);
    }
}
