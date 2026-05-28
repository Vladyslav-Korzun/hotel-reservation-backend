package com.hotel.management.domain.service.guest;

import com.hotel.management.domain.guest.Guest;
import com.hotel.management.domain.guest.GuestRepository;
import com.hotel.management.domain.shared.exception.ForbiddenException;
import com.hotel.management.domain.shared.security.AuthenticatedUser;
import com.hotel.management.domain.shared.value.EmailAddress;

import java.util.Optional;

public class GuestService implements GuestFacade {

    private final GuestRepository guestRepository;

    public GuestService(GuestRepository guestRepository) {
        this.guestRepository = guestRepository;
    }

    @Override
    public Guest resolveGuest(AuthenticatedUser actor) {
        requireActor(actor).requireGuest();
        String keycloakId = actor.subject();
        if (keycloakId == null || keycloakId.isBlank()) {
            throw new ForbiddenException("keycloak subject claim is required");
        }
        return guestRepository.findByKeycloakId(keycloakId)
                .or(() -> bindLegacyGuest(actor.guestId(), keycloakId))
                .or(() -> bindByEmail(actor.email(), keycloakId))
                .orElseGet(() -> guestRepository.save(
                        Guest.register(keycloakId, actor.email(), actor.firstName(), actor.lastName())));
    }

    private Optional<Guest> bindByEmail(String email, String keycloakId) {
        if (email == null || email.isBlank()) return Optional.empty();
        return guestRepository.findByEmail(new EmailAddress(email))
                .filter(guest -> guest.keycloakId() == null)
                .map(guest -> guestRepository.save(guest.withKeycloakId(keycloakId)));
    }

    private Optional<Guest> bindLegacyGuest(Long legacyGuestId, String keycloakId) {
        if (legacyGuestId == null) return Optional.empty();
        return guestRepository.findById(legacyGuestId)
                .map(guest -> guest.keycloakId() == null
                        ? guestRepository.save(guest.withKeycloakId(keycloakId))
                        : guest);
    }

    private static AuthenticatedUser requireActor(AuthenticatedUser actor) {
        if (actor == null) {
            throw new ForbiddenException("authentication is required");
        }
        return actor;
    }
}
