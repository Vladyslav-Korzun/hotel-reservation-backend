package com.hotel.management.jpa.serviceoffering;

import java.math.BigDecimal;

public class JpaServiceOfferingEntity {

    private Long id;
    private Long hotelId;
    private String code;
    private String name;
    private String description;
    private BigDecimal priceAmount;
    private String priceCurrency;
    private Boolean active;
    private String availabilityRule;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getHotelId() {
        return hotelId;
    }

    public void setHotelId(Long hotelId) {
        this.hotelId = hotelId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPriceAmount() {
        return priceAmount;
    }

    public void setPriceAmount(BigDecimal priceAmount) {
        this.priceAmount = priceAmount;
    }

    public String getPriceCurrency() {
        return priceCurrency;
    }

    public void setPriceCurrency(String priceCurrency) {
        this.priceCurrency = priceCurrency;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getAvailabilityRule() {
        return availabilityRule;
    }

    public void setAvailabilityRule(String availabilityRule) {
        this.availabilityRule = availabilityRule;
    }
}
