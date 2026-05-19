package com.hotel.management.domain.shared.security;

import java.util.Set;

public record AuthenticatedUser(String userId, Set<String> roles, Long guestId) {

    public AuthenticatedUser(String userId, Set<String> roles) {
        this(userId, roles, null);
    }

    public boolean hasRole(String role) {
        return roles != null && roles.contains(role);
    }

    public boolean isAdmin() {
        return hasRole("ADMIN");
    }

    public boolean isStaff() {
        return hasRole("STAFF");
    }

    public boolean isGuest() {
        return hasRole("GUEST");
    }
}
