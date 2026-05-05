package com.hotel.management;

import com.hotel.management.service.accommodation.AccommodationPolicyValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AccommodationBeanConfiguration {

    @Bean
    AccommodationPolicyValidator accommodationPolicyValidator() {
        return new AccommodationPolicyValidator();
    }
}
