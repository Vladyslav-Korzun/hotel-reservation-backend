package com.hotel.management.jpa.reservation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "reservations")
public class JpaReservationEntity {

    @Id
    @Column(name = "id", nullable = false, length = 64)
    private String id;

    @Column(name = "hotel_id", nullable = false)
    private Long hotelId;

    @Column(name = "guest_id", nullable = false)
    private Long guestId;

    @Column(name = "room_id")
    private Long roomId;

    @Column(name = "room_type_id", nullable = false)
    private Long roomTypeId;

    @Column(name = "check_in", nullable = false)
    private LocalDate checkIn;

    @Column(name = "check_out", nullable = false)
    private LocalDate checkOut;

    @Column(name = "adults_count", nullable = false)
    private Integer adultsCount;

    @Column(name = "children_ages_json", nullable = false, length = 2000)
    private String childrenAgesJson;

    @Column(name = "pets_json", nullable = false, length = 4000)
    private String petsJson;

    @Column(name = "contact_email", length = 255)
    private String contactEmail;

    @Column(name = "contact_phone", length = 32)
    private String contactPhone;

    @Column(name = "special_requests", length = 500)
    private String specialRequests;

    @Column(name = "base_price_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal basePriceAmount;

    @Column(name = "base_price_currency", nullable = false, length = 3)
    private String basePriceCurrency;

    @Column(name = "services_price_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal servicesPriceAmount;

    @Column(name = "services_price_currency", nullable = false, length = 3)
    private String servicesPriceCurrency;

    @Column(name = "discount_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "discount_currency", nullable = false, length = 3)
    private String discountCurrency;

    @Column(name = "final_price_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal finalPriceAmount;

    @Column(name = "final_price_currency", nullable = false, length = 3)
    private String finalPriceCurrency;

    @Column(name = "status", nullable = false, length = 32)
    private String status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @Column(name = "created_by", nullable = false, length = 128)
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
