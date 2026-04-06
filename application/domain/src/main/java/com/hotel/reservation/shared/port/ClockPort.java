package com.hotel.reservation.shared.port;

import java.time.Instant;

public interface ClockPort {

    Instant now();
}
