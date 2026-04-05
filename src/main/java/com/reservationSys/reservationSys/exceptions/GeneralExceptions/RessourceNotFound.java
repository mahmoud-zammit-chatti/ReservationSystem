package com.reservationSys.reservationSys.exceptions.GeneralExceptions;

public class RessourceNotFound extends RuntimeException{
    public RessourceNotFound(String message) {
        super(message);
    }
}
