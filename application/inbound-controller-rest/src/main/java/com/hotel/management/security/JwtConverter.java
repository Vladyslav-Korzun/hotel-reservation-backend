package com.hotel.management.security;

import com.hotel.management.domain.shared.security.AuthenticatedUser;
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
        return new AuthenticatedUser(extractUserId(source), extractRoles(source), extractGuestId(source));
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

    private static Long extractGuestId(Jwt source) {
        Object claim = source.getClaims().get("guest_id");
        if (claim == null) {
            claim = source.getClaims().get("guestId");
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