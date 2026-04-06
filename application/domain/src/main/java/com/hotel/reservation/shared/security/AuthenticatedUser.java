package com.hotel.reservation.shared.security;

import java.util.Set;

public record AuthenticatedUser(String userId, Set<String> roles) {
}
