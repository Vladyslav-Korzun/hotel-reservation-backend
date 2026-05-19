package com.hotel.management.domain.shared;

public interface NotificationPort {

    void reservationCreated(String reservationId);

    void reservationCancelled(String reservationId);
}
