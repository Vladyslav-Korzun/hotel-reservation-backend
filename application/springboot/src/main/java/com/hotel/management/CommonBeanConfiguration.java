package com.hotel.management;

import com.hotel.management.service.port.ClockPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Instant;

@Configuration
public class CommonBeanConfiguration {

    @Bean
    ClockPort clockPort() {
        return Instant::now;
    }
}
