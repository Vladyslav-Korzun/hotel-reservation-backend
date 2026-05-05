package com.hotel.management.jpa.reservation;

import jakarta.persistence.Entity;

import java.time.Instant;
import java.time.LocalDate;

@Entity
public class JpaReservationEntity {

    private String id;
    private Long hotelId;
    private Long roomId;
    private Long roomTypeId;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private Integer adultsCount;
    private String childrenAgesJson;
    private String petsJson;
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
