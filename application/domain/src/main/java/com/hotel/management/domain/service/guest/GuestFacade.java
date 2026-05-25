package com.hotel.management.domain.service.guest;

public interface GuestFacade {

    /**
     * Finds an existing guest by Keycloak UUID, or creates one from the provided JWT claims.
     * Also handles legacy accounts by binding keycloakId on first login.
     *
     * @param keycloakId    Keycloak subject UUID — always present in JWT sub claim
     * @param legacyGuestId guest_id from JWT claim — present only for pre-created accounts
     * @param email         email claim from JWT
     * @param firstName     given_name claim from JWT
     * @param lastName      family_name claim from JWT
     * @return the resolved guest DB id
     */
    Long findOrCreateByKeycloakId(String keycloakId, Long legacyGuestId,
                                   String email, String firstName, String lastName);
}
