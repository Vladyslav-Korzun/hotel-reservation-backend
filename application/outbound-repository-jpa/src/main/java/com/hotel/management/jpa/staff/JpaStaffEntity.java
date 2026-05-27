package com.hotel.management.jpa.staff;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "staff")
public class JpaStaffEntity {

    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "external_id", nullable = false, length = 255)
    private String externalId;

    @Column(name = "hotel_id")
    private Long hotelId;

    @Column(name = "username", length = 255)
    private String username;

    @Column(name = "email", length = 255)
    private String email;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public Long getHotelId() {
        return hotelId;
    }

    public void setHotelId(Long hotelId) {
        this.hotelId = hotelId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
