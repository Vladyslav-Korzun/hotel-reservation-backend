package com.hotel.management.domain.staff;

import java.util.List;
import java.util.Optional;

public interface StaffRepository {

    Optional<Staff> findById(Long staffId);

    Optional<Staff> findByExternalId(String externalId);

    List<Staff> findAll();

    Staff save(Staff staff);
}
