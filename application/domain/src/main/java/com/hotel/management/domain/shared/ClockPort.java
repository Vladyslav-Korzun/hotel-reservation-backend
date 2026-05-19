package com.hotel.management.domain.shared;

import java.time.Instant;

public interface ClockPort {

    Instant now();
}
