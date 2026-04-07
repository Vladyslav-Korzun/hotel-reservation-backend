package com.hotel.reservation.security;

import com.hotel.reservation.shared.security.AuthenticatedUser;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

class JwtConverter extends AbstractAuthenticationToken {

    private final Jwt source;

    JwtConverter(Jwt source) {
        super(toAuthorities(source));
        this.source = Objects.requireNonNull(source);
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return "";
    }

    @Override
    public Object getPrincipal() {
        return new AuthenticatedUser(extractUserId(source), extractRoles(source));
    }

    private static Collection<? extends GrantedAuthority> toAuthorities(Jwt source) {
        return extractRoles(source).stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .toList();
    }

    private static Set<String> extractRoles(Jwt source) {
        Map<String, Object> realmAccess = source.getClaimAsMap("realm_access");
        if (realmAccess != null && realmAccess.get("roles") instanceof List<?> roles) {
            return roles.stream()
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .collect(java.util.stream.Collectors.toSet());
        }

        List<String> directRoles = source.getClaimAsStringList("roles");
        if (directRoles != null) {
            return Set.copyOf(directRoles);
        }

        return Set.of();
    }

    private static String extractUserId(Jwt source) {
        String preferredUsername = source.getClaimAsString("preferred_username");
        if (preferredUsername != null && !preferredUsername.isBlank()) {
            return preferredUsername;
        }
        return source.getSubject();
    }
}
