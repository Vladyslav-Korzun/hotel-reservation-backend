package com.hotel.management.service.port;

import java.time.Instant;

public interface ClockPort {

    Instant now();
}
