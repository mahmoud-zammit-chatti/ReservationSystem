package com.reservationSys.reservationSys.Exceptions.CarExceptions;

public class CarAlreadyVerifiedException extends RuntimeException {
    public CarAlreadyVerifiedException(String message) {
        super(message);
    }
}
