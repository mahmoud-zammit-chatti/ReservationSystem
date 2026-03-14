package com.reservationSys.reservationSys.Utils;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class TeslaVerificationApi {

    @Async
    public boolean verifyCarWithTesla(String plateNumber, String chassisNumber) {
        // Simulate Tesla API verification logic
        // In a real implementation, this would involve making an HTTP request to Tesla's API
        // and processing the response to determine if the car is verified.
        //and this has to be an Async method so we don't block the flow of adding a new car to the system

        // For demonstration purposes, we'll assume that any car with a non-empty plate number
        // and chassis number is verified successfully.
        return !plateNumber.isEmpty() && !chassisNumber.isEmpty();
    }
}
