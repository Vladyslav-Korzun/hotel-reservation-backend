package com.hotel.management.security;

import com.hotel.management.domain.shared.security.AuthenticatedUser;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JwtConverterTest {

    @Test
    void shouldExposeGuestIdClaimOnAuthenticatedUserPrincipal() {
        Jwt jwt = baseJwt()
                .claim("preferred_username", "guest1")
                .claim("roles", List.of("GUEST"))
                .claim("guest_id", 10L)
                .build();

        var authentication = new JwtConverter(jwt);

        assertThat(authentication.getPrincipal())
                .isInstanceOfSatisfying(AuthenticatedUser.class, user -> {
                    assertThat(user.userId()).isEqualTo("guest1");
                    assertThat(user.roles()).containsExactly("GUEST");
                    assertThat(user.guestId()).isEqualTo(10L);
                });
    }

    @Test
    void shouldAcceptCamelCaseGuestIdClaim() {
        Jwt jwt = baseJwt()
                .claim("preferred_username", "guest1")
                .claim("realm_access", Map.of("roles", List.of("GUEST")))
                .claim("guestId", "10")
                .build();

        var authentication = new JwtConverter(jwt);

        assertThat(authentication.getPrincipal())
                .isInstanceOfSatisfying(AuthenticatedUser.class, user -> {
                    assertThat(user.roles()).containsExactly("GUEST");
                    assertThat(user.guestId()).isEqualTo(10L);
                });
    }

    @Test
    void shouldAcceptSingleValueGuestIdClaimList() {
        Jwt jwt = baseJwt()
                .claim("preferred_username", "guest1")
                .claim("roles", List.of("GUEST"))
                .claim("guest_id", List.of("11"))
                .build();

        var authentication = new JwtConverter(jwt);

        assertThat(authentication.getPrincipal())
                .isInstanceOfSatisfying(AuthenticatedUser.class, user -> assertThat(user.guestId()).isEqualTo(11L));
    }

    private Jwt.Builder baseJwt() {
        return Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject("subject-1")
                .issuedAt(Instant.parse("2026-05-17T10:00:00Z"))
                .expiresAt(Instant.parse("2026-05-17T11:00:00Z"));
    }
}