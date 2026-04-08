package com.reservationSys.reservationSys.Exceptions.AuthExceptions;

public class SmsDeliveryException extends RuntimeException {
    public SmsDeliveryException(String message) {
        super(message);
    }
}
