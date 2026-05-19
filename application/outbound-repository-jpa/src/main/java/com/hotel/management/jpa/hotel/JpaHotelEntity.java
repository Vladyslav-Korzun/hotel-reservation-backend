package com.hotel.management.jpa.hotel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "hotels")
public class JpaHotelEntity {

    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name", nullable = false, length = 160)
    private String name;

    @Column(name = "city", nullable = false, length = 120)
    private String city;

    @Column(name = "country", nullable = false, length = 120)
    private String country;

    @Column(name = "address", nullable = false, length = 255)
    private String address;

    @Column(name = "stars", nullable = false)
    private Integer stars;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "status", nullable = false, length = 32)
    private String status;

    @Column(name = "children_allowed", nullable = false)
    private Boolean childrenAllowed;

    @Column(name = "pets_allowed", nullable = false)
    private Boolean petsAllowed;

    @Column(name = "infant_max_age", nullable = false)
    private Integer infantMaxAge;

    @Column(name = "child_max_age", nullable = false)
    private Integer childMaxAge;

    @Column(name = "adult_equivalent_age", nullable = false)
    private Integer adultEquivalentAge;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Integer getStars() {
        return stars;
    }

    public void setStars(Integer stars) {
        this.stars = stars;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getChildrenAllowed() {
        return childrenAllowed;
    }

    public void setChildrenAllowed(Boolean childrenAllowed) {
        this.childrenAllowed = childrenAllowed;
    }

    public Boolean getPetsAllowed() {
        return petsAllowed;
    }

    public void setPetsAllowed(Boolean petsAllowed) {
        this.petsAllowed = petsAllowed;
    }

    public Integer getInfantMaxAge() {
        return infantMaxAge;
    }

    public void setInfantMaxAge(Integer infantMaxAge) {
        this.infantMaxAge = infantMaxAge;
    }

    public Integer getChildMaxAge() {
        return childMaxAge;
    }

    public void setChildMaxAge(Integer childMaxAge) {
        this.childMaxAge = childMaxAge;
    }

    public Integer getAdultEquivalentAge() {
        return adultEquivalentAge;
    }

    public void setAdultEquivalentAge(Integer adultEquivalentAge) {
        this.adultEquivalentAge = adultEquivalentAge;
    }
}
