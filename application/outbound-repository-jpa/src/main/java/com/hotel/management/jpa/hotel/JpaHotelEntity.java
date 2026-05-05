package com.hotel.management.jpa.hotel;

public class JpaHotelEntity {

    private Long id;
    private String name;
    private String city;
    private String country;
    private String address;
    private Integer stars;
    private String description;
    private String status;
    private Boolean childrenAllowed;
    private Boolean petsAllowed;
    private Integer infantMaxAge;
    private Integer childMaxAge;

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
}
