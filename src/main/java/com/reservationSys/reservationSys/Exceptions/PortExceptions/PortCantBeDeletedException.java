package com.reservationSys.reservationSys.Exceptions.PortExceptions;

public class PortCantBeDeletedException extends RuntimeException {
    public PortCantBeDeletedException(String message) {
        super(message);
    }
}
