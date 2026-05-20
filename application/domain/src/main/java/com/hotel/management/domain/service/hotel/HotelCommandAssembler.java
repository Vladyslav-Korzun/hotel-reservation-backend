package com.hotel.management.domain.service.hotel;

import com.hotel.management.domain.hotel.HotelPolicy;

final class HotelCommandAssembler {

    private HotelCommandAssembler() {
    }

    static HotelPolicy toPolicy(CreateHotelCommand command) {
        return new HotelPolicy(
                command.childrenAllowed(),
                command.petsAllowed(),
                command.infantMaxAge(),
                command.childMaxAge(),
                command.adultEquivalentAge()
        );
    }

    static HotelPolicy toPolicy(UpdateHotelCommand command) {
        return new HotelPolicy(
                command.childrenAllowed(),
                command.petsAllowed(),
                command.infantMaxAge(),
                command.childMaxAge(),
                command.adultEquivalentAge()
        );
    }
}
