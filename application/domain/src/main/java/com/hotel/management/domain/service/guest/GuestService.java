package com.hotel.management.domain.service.guest;

import com.hotel.management.domain.guest.Guest;
import com.hotel.management.domain.guest.GuestRepository;
import com.hotel.management.domain.shared.value.EmailAddress;

public class GuestService implements GuestFacade {

    private final GuestRepository guestRepository;

    public GuestService(GuestRepository guestRepository) {
        this.guestRepository = guestRepository;
    }

    @Override
    public Long findOrCreateByKeycloakId(String keycloakId, Long legacyGuestId,
                                          String email, String firstName, String lastName) {
        // 1. Primary: find by Keycloak UUID (fast path for returning users)
        if (keycloakId != null) {
            var byKeycloak = guestRepository.findByKeycloakId(keycloakId);
            if (byKeycloak.isPresent()) {
                return byKeycloak.get().id();
            }
        }

        // 2. Fallback: legacy demo accounts that have guest_id claim in JWT
        if (legacyGuestId != null) {
            var byId = guestRepository.findById(legacyGuestId);
            if (byId.isPresent()) {
                Guest guest = byId.get();
                // Bind keycloakId so future logins skip this fallback
                if (keycloakId != null && guest.keycloakId() == null) {
                    guest = guestRepository.save(guest.withKeycloakId(keycloakId));
                }
                return guest.id();
            }
        }

        // 3. Check by email to avoid duplicate guest records
        if (email != null && !email.isBlank()) {
            try {
                var byEmail = guestRepository.findByEmail(new EmailAddress(email));
                if (byEmail.isPresent()) {
                    Guest guest = byEmail.get();
                    if (keycloakId != null && guest.keycloakId() == null) {
                        guest = guestRepository.save(guest.withKeycloakId(keycloakId));
                    }
                    return guest.id();
                }
            } catch (Exception ignored) {
                // invalid email format — skip email lookup
            }
        }

        // 4. Create new guest record for self-registered Keycloak users
        String resolvedEmail    = email != null && !email.isBlank() ? email : keycloakId + "@keycloak.local";
        String resolvedFirst    = firstName != null && !firstName.isBlank() ? firstName : firstNameFrom(resolvedEmail);
        String resolvedLast     = lastName  != null && !lastName.isBlank()  ? lastName  : "User";

        return guestRepository.save(
                new Guest(null, resolvedFirst, resolvedLast, new EmailAddress(resolvedEmail), null, keycloakId)
        ).id();
    }

    private static String firstNameFrom(String email) {
        if (!email.contains("@")) {
            return "Guest";
        }
        String local = email.split("@")[0];
        return Character.toUpperCase(local.charAt(0)) + local.substring(1);
    }
}
