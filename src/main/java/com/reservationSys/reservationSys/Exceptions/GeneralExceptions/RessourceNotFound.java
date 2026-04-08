package com.reservationSys.reservationSys.Exceptions.GeneralExceptions;

public class RessourceNotFound extends RuntimeException{
    public RessourceNotFound(String message) {
        super(message);
    }
}
