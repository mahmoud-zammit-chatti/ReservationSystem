package com.reservationSys.reservationSys.Exceptions.GeneralExceptions;

public class ResourceNotFound extends RuntimeException{
    public ResourceNotFound(String message) {
        super(message);
    }
}
