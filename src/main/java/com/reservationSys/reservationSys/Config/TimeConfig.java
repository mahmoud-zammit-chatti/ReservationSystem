package com.reservationSys.reservationSys.Config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class TimeConfig {

    //testtttttttttt
    @Bean
    public ZoneId businessZoneID() {
        return ZoneId.of("Africa/Tunis");
    }

    @Bean
    public Clock businessClock(ZoneId businessZoneID) {
        return Clock.system(businessZoneID);
    }
}
