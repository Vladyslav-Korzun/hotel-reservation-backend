package com.hotel.management.domain.shared.security;

import com.hotel.management.domain.shared.exception.ForbiddenException;

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

    public void requireAdmin() {
        if (!isAdmin()) {
            throw new ForbiddenException("Admin role required");
        }
    }

    public void requireStaff() {
        if (!isStaff()) {
            throw new ForbiddenException("Staff role required");
        }
    }

    public void requireStaffOrAdmin() {
        if (!isStaff() && !isAdmin()) {
            throw new ForbiddenException("Staff or admin role required");
        }
    }

    public void requireGuest() {
        if (!isGuest()) {
            throw new ForbiddenException("Guest role required");
        }
    }

    public Long requireGuestId() {
        requireGuest();
        if (guestId == null) {
            throw new ForbiddenException("guestId claim is required");
        }
        return guestId;
    }
}
