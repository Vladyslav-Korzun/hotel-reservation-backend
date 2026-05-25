package com.hotel.management;

import com.hotel.management.domain.guest.GuestRepository;
import com.hotel.management.domain.service.guest.GuestFacade;
import com.hotel.management.domain.service.guest.GuestService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GuestBeanConfiguration {

    @Bean
    GuestFacade guestFacade(GuestRepository guestRepository) {
        return new TransactionalGuestFacade(new GuestService(guestRepository));
    }
}
