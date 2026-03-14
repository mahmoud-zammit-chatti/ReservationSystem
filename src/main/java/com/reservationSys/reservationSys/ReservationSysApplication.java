package com.reservationSys.reservationSys;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ReservationSysApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReservationSysApplication.class, args);
	}

}
