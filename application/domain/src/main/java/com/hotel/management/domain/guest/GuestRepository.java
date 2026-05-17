package com.hotel.management.domain.guest;

import com.hotel.management.domain.shared.value.EmailAddress;

import java.util.Optional;

public interface GuestRepository {

    Optional<Guest> findById(Long guestId);

    Optional<Guest> findByEmail(EmailAddress email);

    Guest save(Guest guest);
}
