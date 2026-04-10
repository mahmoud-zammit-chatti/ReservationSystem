package com.reservationSys.reservationSys.Services.Car;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class CarVerificationListener {

    private final CarVerificationService carVerificationService;

    public CarVerificationListener(CarVerificationService carVerificationService) {
        this.carVerificationService = carVerificationService;
    }
}
