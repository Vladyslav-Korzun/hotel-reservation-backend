package com.hotel.management.jpa.room;

import java.math.BigDecimal;

public class JpaRoomTypeEntity {

    private Long id;
    private Long hotelId;
    private String name;
    private Integer maxAdults;
    private Integer maxChildren;
    private Integer maxInfants;
    private Integer maxTotalGuests;
    private Boolean petsAllowed;
    private Integer maxPets;
    private String allowedPetTypesJson;
    private BigDecimal maxPetWeightKg;
    private BigDecimal petFeeAmount;
    private String petFeeCurrency;
    private BigDecimal basePriceAmount;
    private String basePriceCurrency;
    private String description;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getMaxAdults() {
        return maxAdults;
    }

    public void setMaxAdults(Integer maxAdults) {
        this.maxAdults = maxAdults;
    }

    public Integer getMaxChildren() {
        return maxChildren;
    }

    public void setMaxChildren(Integer maxChildren) {
        this.maxChildren = maxChildren;
    }

    public Integer getMaxInfants() {
        return maxInfants;
    }

    public void setMaxInfants(Integer maxInfants) {
        this.maxInfants = maxInfants;
    }

    public Integer getMaxTotalGuests() {
        return maxTotalGuests;
    }

    public void setMaxTotalGuests(Integer maxTotalGuests) {
        this.maxTotalGuests = maxTotalGuests;
    }

    public Boolean getPetsAllowed() {
        return petsAllowed;
    }

    public void setPetsAllowed(Boolean petsAllowed) {
        this.petsAllowed = petsAllowed;
    }

    public Integer getMaxPets() {
        return maxPets;
    }

    public void setMaxPets(Integer maxPets) {
        this.maxPets = maxPets;
    }

    public String getAllowedPetTypesJson() {
        return allowedPetTypesJson;
    }

    public void setAllowedPetTypesJson(String allowedPetTypesJson) {
        this.allowedPetTypesJson = allowedPetTypesJson;
    }

    public BigDecimal getMaxPetWeightKg() {
        return maxPetWeightKg;
    }

    public void setMaxPetWeightKg(BigDecimal maxPetWeightKg) {
        this.maxPetWeightKg = maxPetWeightKg;
    }

    public BigDecimal getPetFeeAmount() {
        return petFeeAmount;
    }

    public void setPetFeeAmount(BigDecimal petFeeAmount) {
        this.petFeeAmount = petFeeAmount;
    }

    public String getPetFeeCurrency() {
        return petFeeCurrency;
    }

    public void setPetFeeCurrency(String petFeeCurrency) {
        this.petFeeCurrency = petFeeCurrency;
    }

    public BigDecimal getBasePriceAmount() {
        return basePriceAmount;
    }

    public void setBasePriceAmount(BigDecimal basePriceAmount) {
        this.basePriceAmount = basePriceAmount;
    }

    public String getBasePriceCurrency() {
        return basePriceCurrency;
    }

    public void setBasePriceCurrency(String basePriceCurrency) {
        this.basePriceCurrency = basePriceCurrency;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
