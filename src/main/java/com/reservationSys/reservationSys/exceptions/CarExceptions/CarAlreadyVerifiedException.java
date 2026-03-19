package com.reservationSys.reservationSys.exceptions.CarExceptions;

public class CarAlreadyVerifiedException extends RuntimeException {
    public CarAlreadyVerifiedException(String message) {
        super(message);
    }
}
