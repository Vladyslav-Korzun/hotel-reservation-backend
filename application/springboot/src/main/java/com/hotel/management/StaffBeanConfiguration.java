package com.hotel.management;

import com.hotel.management.domain.hotel.HotelRepository;
import com.hotel.management.domain.service.staff.HotelScopePolicy;
import com.hotel.management.domain.service.staff.StaffFacade;
import com.hotel.management.domain.service.staff.StaffService;
import com.hotel.management.domain.staff.StaffRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StaffBeanConfiguration {

    @Bean
    StaffFacade staffFacade(StaffRepository staffRepository, HotelRepository hotelRepository) {
        return new TransactionalStaffFacade(new StaffService(staffRepository, hotelRepository));
    }

    @Bean
    HotelScopePolicy hotelScopePolicy(StaffFacade staffFacade) {
        return new HotelScopePolicy(staffFacade);
    }
}
