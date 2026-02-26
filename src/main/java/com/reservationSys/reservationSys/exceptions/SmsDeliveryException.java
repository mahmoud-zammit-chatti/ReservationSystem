package com.reservationSys.reservationSys.exceptions;

public class SmsDeliveryException extends RuntimeException {
    public SmsDeliveryException(String message) {
        super(message);
    }
}
