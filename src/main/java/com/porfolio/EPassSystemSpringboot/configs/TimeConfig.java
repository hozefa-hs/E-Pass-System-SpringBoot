package com.porfolio.EPassSystemSpringboot.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class TimeConfig {

    @Value("${business-time-zone}")
    String businessTimeZone;

    @Bean
    public Clock businessClock() {
        return Clock.system(ZoneId.of(businessTimeZone));
    }

}
