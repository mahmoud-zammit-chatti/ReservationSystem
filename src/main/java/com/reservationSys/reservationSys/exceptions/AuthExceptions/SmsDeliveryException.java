package com.reservationSys.reservationSys.exceptions.AuthExceptions;

public class SmsDeliveryException extends RuntimeException {
    public SmsDeliveryException(String message) {
        super(message);
    }
}
