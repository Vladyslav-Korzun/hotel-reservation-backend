package com.hotel.reservation.security;

import com.hotel.reservation.shared.port.CurrentUserPort;
import com.hotel.reservation.shared.security.AuthenticatedUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class SpringSecurityCurrentUserAdapter implements CurrentUserPort {

    @Override
    public AuthenticatedUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();

        if (principal instanceof AuthenticatedUser authenticatedUser) {
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
            return new AuthenticatedUser(userId, roles);
        }

        throw new IllegalStateException("Authenticated JWT principal is required");
    }
}
