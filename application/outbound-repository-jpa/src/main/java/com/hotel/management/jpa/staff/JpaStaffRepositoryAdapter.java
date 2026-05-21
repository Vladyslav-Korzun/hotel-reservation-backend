package com.hotel.management.jpa.staff;

import com.hotel.management.domain.staff.Staff;
import com.hotel.management.domain.staff.StaffRepository;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class JpaStaffRepositoryAdapter implements StaffRepository {

    private final JpaStaffSpringDataRepository repository;
    private final EntityManager entityManager;

    public JpaStaffRepositoryAdapter(JpaStaffSpringDataRepository repository, EntityManager entityManager) {
        this.repository = repository;
        this.entityManager = entityManager;
    }

    @Override
    public Optional<Staff> findById(Long staffId) {
        return repository.findById(staffId).map(this::toDomain);
    }

    @Override
    public Optional<Staff> findByExternalId(String externalId) {
        return repository.findByExternalId(externalId).map(this::toDomain);
    }

    @Override
    public List<Staff> findAll() {
        return repository.findAll().stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Staff save(Staff staff) {
        Staff staffWithId = staff.id() == null ? withGeneratedId(staff) : staff;
        return toDomain(repository.save(toEntity(staffWithId)));
    }

    private Staff withGeneratedId(Staff staff) {
        Number nextId = (Number) entityManager
                .createNativeQuery("select nextval('staff_id_seq')")
                .getSingleResult();
        return new Staff(nextId.longValue(), staff.externalId(), staff.hotelId());
    }

    private Staff toDomain(JpaStaffEntity entity) {
        return new Staff(entity.getId(), entity.getExternalId(), entity.getHotelId());
    }

    private JpaStaffEntity toEntity(Staff staff) {
        var entity = new JpaStaffEntity();
        entity.setId(staff.id());
        entity.setExternalId(staff.externalId());
        entity.setHotelId(staff.hotelId());
        return entity;
    }
}
