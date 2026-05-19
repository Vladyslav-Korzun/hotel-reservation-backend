package com.hotel.management.jpa.room;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "room_types")
public class JpaRoomTypeEntity {

    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "hotel_id", nullable = false)
    private Long hotelId;

    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Column(name = "max_adults", nullable = false)
    private Integer maxAdults;

    @Column(name = "max_children", nullable = false)
    private Integer maxChildren;

    @Column(name = "max_infants", nullable = false)
    private Integer maxInfants;

    @Column(name = "max_total_guests", nullable = false)
    private Integer maxTotalGuests;

    @Column(name = "pets_allowed", nullable = false)
    private Boolean petsAllowed;

    @Column(name = "max_pets", nullable = false)
    private Integer maxPets;

    @Column(name = "allowed_pet_types_json", nullable = false, length = 2000)
    private String allowedPetTypesJson;

    @Column(name = "max_pet_weight_kg", precision = 10, scale = 2)
    private BigDecimal maxPetWeightKg;

    @Column(name = "pet_fee_amount", precision = 12, scale = 2)
    private BigDecimal petFeeAmount;

    @Column(name = "pet_fee_currency", length = 3)
    private String petFeeCurrency;

    @Column(name = "base_price_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal basePriceAmount;

    @Column(name = "base_price_currency", nullable = false, length = 3)
    private String basePriceCurrency;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "bed_setup", length = 120)
    private String bedSetup;

    @Column(name = "room_size_sqm", precision = 5, scale = 1)
    private BigDecimal roomSizeSqm;

    @Column(name = "amenities_json", nullable = false, length = 4000)
    private String amenitiesJson = "[]";

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

    public String getBedSetup() {
        return bedSetup;
    }

    public void setBedSetup(String bedSetup) {
        this.bedSetup = bedSetup;
    }

    public BigDecimal getRoomSizeSqm() {
        return roomSizeSqm;
    }

    public void setRoomSizeSqm(BigDecimal roomSizeSqm) {
        this.roomSizeSqm = roomSizeSqm;
    }

    public String getAmenitiesJson() {
        return amenitiesJson;
    }

    public void setAmenitiesJson(String amenitiesJson) {
        this.amenitiesJson = amenitiesJson;
    }
}
