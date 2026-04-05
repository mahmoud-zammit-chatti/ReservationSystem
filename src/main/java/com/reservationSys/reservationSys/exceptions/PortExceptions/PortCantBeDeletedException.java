package com.reservationSys.reservationSys.exceptions.PortExceptions;

public class PortCantBeDeletedException extends RuntimeException {
    public PortCantBeDeletedException(String message) {
        super(message);
    }
}
