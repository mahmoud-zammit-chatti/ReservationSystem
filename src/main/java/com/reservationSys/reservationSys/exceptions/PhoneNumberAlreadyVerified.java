package com.reservationSys.reservationSys.exceptions;

public class PhoneNumberAlreadyVerified extends RuntimeException {
    public PhoneNumberAlreadyVerified(String message) {
        super(message);
    }
}
