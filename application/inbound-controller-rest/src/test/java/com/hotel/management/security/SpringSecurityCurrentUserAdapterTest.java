package com.hotel.management.security;

import com.hotel.management.domain.guest.Guest;
import com.hotel.management.domain.service.guest.GuestFacade;
import com.hotel.management.domain.shared.security.AuthenticatedUser;
import com.hotel.management.domain.shared.value.EmailAddress;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SpringSecurityCurrentUserAdapterTest {

    private final TestGuestFacade guestFacade = new TestGuestFacade();
    private final SpringSecurityCurrentUserAdapter adapter = new SpringSecurityCurrentUserAdapter(guestFacade);

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldResolveGuestIdForGuestRole() {
        AuthenticatedUser guestActor = new AuthenticatedUser(
                "guest-1", Set.of("GUEST"), (Long) null, "kc-uuid-1", "a@b.com", "Alice", "Smith");
        guestFacade.resolved = new Guest(42L, "Alice", "Smith", new EmailAddress("a@b.com"), null, "kc-uuid-1");
        setSecurityContext(guestActor);

        AuthenticatedUser result = adapter.getCurrentUser();

        assertThat(result.guestId()).isEqualTo(42L);
        assertThat(result.subject()).isEqualTo("kc-uuid-1");
        assertThat(result.email()).isEqualTo("a@b.com");
        assertThat(guestFacade.lastActor).isSameAs(guestActor);
    }

    @Test
    void shouldReturnActorAsIsForNonGuestRole() {
        AuthenticatedUser admin = new AuthenticatedUser("admin-1", Set.of("ADMIN"), (Long) null, "kc-uuid-admin");
        setSecurityContext(admin);

        AuthenticatedUser result = adapter.getCurrentUser();

        assertThat(result).isSameAs(admin);
        assertThat(guestFacade.lastActor).isNull();
    }

    @Test
    void shouldReturnTrueForAnonymousAuthentication() {
        SecurityContextHolder.getContext().setAuthentication(
                new AnonymousAuthenticationToken("key", "anonymous",
                        List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))));

        assertThat(adapter.isAnonymous()).isTrue();
    }

    @Test
    void shouldReturnFalseForAuthenticatedUser() {
        AuthenticatedUser admin = new AuthenticatedUser("admin-1", Set.of("ADMIN"));
        setSecurityContext(admin);

        assertThat(adapter.isAnonymous()).isFalse();
    }

    @Test
    void shouldThrowWhenPrincipalIsNotRecognized() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("unknown", "password"));

        assertThatThrownBy(() -> adapter.getCurrentUser())
                .isInstanceOf(IllegalStateException.class);
    }

    private static void setSecurityContext(AuthenticatedUser user) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, List.of()));
    }

    private static final class TestGuestFacade implements GuestFacade {

        Guest resolved;
        AuthenticatedUser lastActor;

        @Override
        public Guest resolveGuest(AuthenticatedUser actor) {
            this.lastActor = actor;
            return resolved;
        }
    }
}
