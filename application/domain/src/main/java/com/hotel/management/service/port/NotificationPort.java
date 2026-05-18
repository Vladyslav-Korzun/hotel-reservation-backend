package com.hotel.management.service.port;

public interface NotificationPort {

    void reservationCreated(String reservationId);

    void reservationCancelled(String reservationId);
}
