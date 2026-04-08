package com.reservationSys.reservationSys.exceptions.PortExceptions;

public class PortNotAvailableException extends RuntimeException {
    public PortNotAvailableException(String message) {
        super(message);
    }
}
