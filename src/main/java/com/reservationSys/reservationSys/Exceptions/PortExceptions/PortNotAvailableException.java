package com.reservationSys.reservationSys.Exceptions.PortExceptions;

public class PortNotAvailableException extends RuntimeException {
    public PortNotAvailableException(String message) {
        super(message);
    }
}
