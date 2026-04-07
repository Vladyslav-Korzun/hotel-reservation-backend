package com.hotel.reservation.shared.security;

import java.util.Set;

public record AuthenticatedUser(String userId, Set<String> roles) {

    public boolean hasRole(String role) {
        return roles != null && roles.contains(role);
    }

    public boolean isAdmin() {
        return hasRole("ADMIN");
    }
}
