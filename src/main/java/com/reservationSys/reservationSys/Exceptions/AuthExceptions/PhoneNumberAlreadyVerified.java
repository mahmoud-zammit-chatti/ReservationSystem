package com.reservationSys.reservationSys.Exceptions.AuthExceptions;

public class PhoneNumberAlreadyVerified extends RuntimeException {
    public PhoneNumberAlreadyVerified(String message) {
        super(message);
    }
}
