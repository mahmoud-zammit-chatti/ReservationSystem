package com.reservationSys.reservationSys.Services.Car;

import org.springframework.context.ApplicationEvent;

import java.util.UUID;



    public record CarCreatedEvent(UUID carId) {
    }



