package com.hotel.management.jpa.reservation;

import jakarta.persistence.Entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
public class JpaReservationEntity {

    private String id;
    private Long hotelId;
    private Long guestId;
    private Long roomId;
    private Long roomTypeId;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private Integer adultsCount;
    private String childrenAgesJson;
    private String petsJson;
    private String contactEmail;
    private String contactPhone;
    private String specialRequests;
    private BigDecimal basePriceAmount;
    private String basePriceCurrency;
    private BigDecimal servicesPriceAmount;
    private String servicesPriceCurrency;
    private BigDecimal discountAmount;
    private String discountCurrency;
    private BigDecimal finalPriceAmount;
    private String finalPriceCurrency;
    private String status;
    private Instant createdAt;
    private Instant cancelledAt;
    private String createdBy;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getHotelId() {
        return hotelId;
    }

    public void setHotelId(Long hotelId) {
        this.hotelId = hotelId;
    }

    public Long getGuestId() {
        return guestId;
    }

    public void setGuestId(Long guestId) {
        this.guestId = guestId;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public Long getRoomTypeId() {
        return roomTypeId;
    }

    public void setRoomTypeId(Long roomTypeId) {
        this.roomTypeId = roomTypeId;
    }

    public LocalDate getCheckIn() {
        return checkIn;
    }

    public void setCheckIn(LocalDate checkIn) {
        this.checkIn = checkIn;
    }

    public LocalDate getCheckOut() {
        return checkOut;
    }

    public void setCheckOut(LocalDate checkOut) {
        this.checkOut = checkOut;
    }

    public Integer getAdultsCount() {
        return adultsCount;
    }

    public void setAdultsCount(Integer adultsCount) {
        this.adultsCount = adultsCount;
    }

    public String getChildrenAgesJson() {
        return childrenAgesJson;
    }

    public void setChildrenAgesJson(String childrenAgesJson) {
        this.childrenAgesJson = childrenAgesJson;
    }

    public String getPetsJson() {
        return petsJson;
    }

    public void setPetsJson(String petsJson) {
        this.petsJson = petsJson;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getSpecialRequests() {
        return specialRequests;
    }

    public void setSpecialRequests(String specialRequests) {
        this.specialRequests = specialRequests;
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

    public BigDecimal getServicesPriceAmount() {
        return servicesPriceAmount;
    }

    public void setServicesPriceAmount(BigDecimal servicesPriceAmount) {
        this.servicesPriceAmount = servicesPriceAmount;
    }

    public String getServicesPriceCurrency() {
        return servicesPriceCurrency;
    }

    public void setServicesPriceCurrency(String servicesPriceCurrency) {
        this.servicesPriceCurrency = servicesPriceCurrency;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public String getDiscountCurrency() {
        return discountCurrency;
    }

    public void setDiscountCurrency(String discountCurrency) {
        this.discountCurrency = discountCurrency;
    }

    public BigDecimal getFinalPriceAmount() {
        return finalPriceAmount;
    }

    public void setFinalPriceAmount(BigDecimal finalPriceAmount) {
        this.finalPriceAmount = finalPriceAmount;
    }

    public String getFinalPriceCurrency() {
        return finalPriceCurrency;
    }

    public void setFinalPriceCurrency(String finalPriceCurrency) {
        this.finalPriceCurrency = finalPriceCurrency;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(Instant cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
}
