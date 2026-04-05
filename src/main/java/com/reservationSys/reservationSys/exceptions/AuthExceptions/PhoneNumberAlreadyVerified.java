package com.reservationSys.reservationSys.exceptions.AuthExceptions;

public class PhoneNumberAlreadyVerified extends RuntimeException {
    public PhoneNumberAlreadyVerified(String message) {
        super(message);
    }
}
