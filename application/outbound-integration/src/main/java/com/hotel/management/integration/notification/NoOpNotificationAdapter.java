package com.hotel.management.integration.notification;

import com.hotel.management.service.port.NotificationPort;
import org.springframework.stereotype.Component;

@Component
public class NoOpNotificationAdapter implements NotificationPort {

    @Override
    public void reservationCreated(String reservationId) {
        // MVP adapter: external notifications are intentionally disabled.
    }

    @Override
    public void reservationCancelled(String reservationId) {
        // MVP adapter: external notifications are intentionally disabled.
    }
}
