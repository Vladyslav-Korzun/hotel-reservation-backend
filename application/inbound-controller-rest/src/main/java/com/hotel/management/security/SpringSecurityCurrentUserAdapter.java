package com.hotel.management.security;

import com.hotel.management.domain.service.guest.GuestFacade;
import com.hotel.management.domain.shared.security.AuthenticatedUser;
import com.hotel.management.domain.shared.security.CurrentUserPort;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class SpringSecurityCurrentUserAdapter implements CurrentUserPort {

    private final GuestFacade guestFacade;

    public SpringSecurityCurrentUserAdapter(GuestFacade guestFacade) {
        this.guestFacade = guestFacade;
    }

    @Override
    public AuthenticatedUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();

        if (principal instanceof AuthenticatedUser authenticatedUser) {
            // For GUEST role: resolve (or auto-create) the DB guest record
            if (authenticatedUser.isGuest()) {
                Long resolvedGuestId = guestFacade.findOrCreateByKeycloakId(
                        authenticatedUser.subject(),
                        authenticatedUser.guestId(),
                        authenticatedUser.email(),
                        authenticatedUser.firstName(),
                        authenticatedUser.lastName()
                );
                return new AuthenticatedUser(
                        authenticatedUser.userId(),
                        authenticatedUser.roles(),
                        resolvedGuestId,
                        authenticatedUser.subject(),
                        authenticatedUser.email(),
                        authenticatedUser.firstName(),
                        authenticatedUser.lastName()
                );
            }
            return authenticatedUser;
        }

        if (principal instanceof Jwt jwt) {
            Set<String> roles = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .map(authority -> authority.startsWith("ROLE_") ? authority.substring(5) : authority)
                    .collect(Collectors.toSet());
            String preferredUsername = jwt.getClaimAsString("preferred_username");
            String userId = preferredUsername != null && !preferredUsername.isBlank()
                    ? preferredUsername
                    : jwt.getSubject();
            return new AuthenticatedUser(userId, roles, guestId(jwt), jwt.getSubject(),
                    jwt.getClaimAsString("email"),
                    jwt.getClaimAsString("given_name"),
                    jwt.getClaimAsString("family_name"));
        }

        throw new IllegalStateException("Authenticated JWT principal is required");
    }

    @Override
    public boolean isAnonymous() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken;
    }

    private Long guestId(Jwt jwt) {
        Object claim = jwt.getClaims().get("guest_id");
        if (claim == null) {
            claim = jwt.getClaims().get("guestId");
        }
        if (claim instanceof Number number) {
            return number.longValue();
        }
        if (claim instanceof String value && !value.isBlank()) {
            return Long.valueOf(value);
        }
        if (claim instanceof List<?> values && !values.isEmpty()) {
            Object firstValue = values.getFirst();
            if (firstValue instanceof Number number) {
                return number.longValue();
            }
            if (firstValue instanceof String value && !value.isBlank()) {
                return Long.valueOf(value);
            }
        }
        return null;
    }
}