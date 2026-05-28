package com.hotel.management.domain.service.guest;

import com.hotel.management.domain.guest.Guest;
import com.hotel.management.domain.guest.GuestRepository;
import com.hotel.management.domain.shared.exception.ForbiddenException;
import com.hotel.management.domain.shared.security.AuthenticatedUser;
import com.hotel.management.domain.shared.value.EmailAddress;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GuestServiceTest {

    @Mock
    private GuestRepository guestRepository;

    private GuestService guestService;

    @BeforeEach
    void setUp() {
        guestService = new GuestService(guestRepository);
    }

    @Test
    void shouldReturnExistingGuestWhenFoundByKeycloakId() {
        Guest existing = dbGuest(10L, "kc-uuid-1");
        when(guestRepository.findByKeycloakId("kc-uuid-1")).thenReturn(Optional.of(existing));

        Guest result = guestService.resolveGuest(actor(null, "kc-uuid-1", "a@b.com", "Alice", "Smith"));

        assertEquals(10L, result.id());
        verify(guestRepository, never()).save(any());
    }

    @Test
    void shouldBindKeycloakIdToLegacyGuestOnFirstLogin() {
        Guest legacy = dbGuest(5L, null);
        when(guestRepository.findByKeycloakId("kc-uuid-2")).thenReturn(Optional.empty());
        when(guestRepository.findById(5L)).thenReturn(Optional.of(legacy));
        when(guestRepository.save(any(Guest.class))).thenAnswer(inv -> inv.getArgument(0));

        Guest result = guestService.resolveGuest(actor(5L, "kc-uuid-2", "b@b.com", "Bob", "Brown"));

        assertEquals(5L, result.id());
        ArgumentCaptor<Guest> captor = ArgumentCaptor.forClass(Guest.class);
        verify(guestRepository).save(captor.capture());
        assertEquals("kc-uuid-2", captor.getValue().keycloakId());
    }

    @Test
    void shouldNotRebindLegacyGuestThatAlreadyHasKeycloakId() {
        Guest alreadyBound = dbGuest(5L, "kc-uuid-other");
        when(guestRepository.findByKeycloakId("kc-uuid-2")).thenReturn(Optional.empty());
        when(guestRepository.findById(5L)).thenReturn(Optional.of(alreadyBound));

        Guest result = guestService.resolveGuest(actor(5L, "kc-uuid-2", "b@b.com", "Bob", "Brown"));

        assertEquals(5L, result.id());
        verify(guestRepository, never()).save(any());
    }

    @Test
    void shouldCreateNewGuestWhenNoKeycloakMatchAndNoLegacyId() {
        when(guestRepository.findByKeycloakId("kc-uuid-3")).thenReturn(Optional.empty());
        when(guestRepository.save(any(Guest.class))).thenAnswer(inv -> {
            Guest g = inv.getArgument(0);
            return new Guest(99L, g.firstName(), g.lastName(), g.email(), g.phone(), g.keycloakId());
        });

        Guest result = guestService.resolveGuest(actor(null, "kc-uuid-3", "c@c.com", "Carol", "White"));

        assertEquals(99L, result.id());
        ArgumentCaptor<Guest> captor = ArgumentCaptor.forClass(Guest.class);
        verify(guestRepository).save(captor.capture());
        assertEquals("kc-uuid-3", captor.getValue().keycloakId());
        assertEquals("Carol", captor.getValue().firstName());
        assertEquals("c@c.com", captor.getValue().email().value());
    }

    @Test
    void shouldCreateNewGuestWhenLegacyIdNotFoundInDatabase() {
        when(guestRepository.findByKeycloakId("kc-uuid-4")).thenReturn(Optional.empty());
        when(guestRepository.findById(99L)).thenReturn(Optional.empty());
        when(guestRepository.save(any(Guest.class))).thenAnswer(inv -> {
            Guest g = inv.getArgument(0);
            return new Guest(100L, g.firstName(), g.lastName(), g.email(), g.phone(), g.keycloakId());
        });

        Guest result = guestService.resolveGuest(actor(99L, "kc-uuid-4", "d@d.com", "Dan", "Black"));

        assertEquals(100L, result.id());
    }

    @Test
    void shouldApplyFallbacksWhenClaimsAreNull() {
        when(guestRepository.findByKeycloakId("kc-uuid-5")).thenReturn(Optional.empty());
        when(guestRepository.save(any(Guest.class))).thenAnswer(inv -> {
            Guest g = inv.getArgument(0);
            return new Guest(101L, g.firstName(), g.lastName(), g.email(), g.phone(), g.keycloakId());
        });

        guestService.resolveGuest(actor(null, "kc-uuid-5", null, null, null));

        ArgumentCaptor<Guest> captor = ArgumentCaptor.forClass(Guest.class);
        verify(guestRepository).save(captor.capture());
        assertEquals("Guest", captor.getValue().firstName());
        assertEquals("User", captor.getValue().lastName());
        assertEquals("kc-uuid-5@keycloak.local", captor.getValue().email().value());
    }

    @Test
    void shouldBindKeycloakIdToGuestFoundByEmail() {
        Guest unlinked = new Guest(42L, "Ivan", "Petrov", new EmailAddress("ivan@mail.com"), null, null);
        when(guestRepository.findByKeycloakId("kc-uuid-new")).thenReturn(Optional.empty());
        when(guestRepository.findByEmail(new EmailAddress("ivan@mail.com"))).thenReturn(Optional.of(unlinked));
        when(guestRepository.save(any(Guest.class))).thenAnswer(inv -> inv.getArgument(0));

        Guest result = guestService.resolveGuest(actor(null, "kc-uuid-new", "ivan@mail.com", "Ivan", "Petrov"));

        assertEquals(42L, result.id());
        ArgumentCaptor<Guest> captor = ArgumentCaptor.forClass(Guest.class);
        verify(guestRepository).save(captor.capture());
        assertEquals("kc-uuid-new", captor.getValue().keycloakId());
    }

    @Test
    void shouldNotLinkByEmailWhenGuestAlreadyHasKeycloakId() {
        Guest alreadyBound = new Guest(42L, "Ivan", "Petrov", new EmailAddress("ivan@mail.com"), null, "kc-uuid-other");
        when(guestRepository.findByKeycloakId("kc-uuid-new")).thenReturn(Optional.empty());
        when(guestRepository.findByEmail(new EmailAddress("ivan@mail.com"))).thenReturn(Optional.of(alreadyBound));
        when(guestRepository.save(any(Guest.class))).thenAnswer(inv -> {
            Guest g = inv.getArgument(0);
            return new Guest(99L, g.firstName(), g.lastName(), g.email(), g.phone(), g.keycloakId());
        });

        Guest result = guestService.resolveGuest(actor(null, "kc-uuid-new", "ivan@mail.com", "Ivan", "Petrov"));

        assertEquals(99L, result.id());
        assertEquals("kc-uuid-new", result.keycloakId());
    }

    @Test
    void shouldThrowWhenActorIsNull() {
        assertThrows(ForbiddenException.class, () -> guestService.resolveGuest(null));
    }

    @Test
    void shouldThrowWhenActorIsNotGuest() {
        AuthenticatedUser staff = new AuthenticatedUser("u", Set.of("STAFF"), null, "kc-sub");
        assertThrows(ForbiddenException.class, () -> guestService.resolveGuest(staff));
    }

    @Test
    void shouldThrowWhenSubjectIsBlank() {
        AuthenticatedUser noSubject = new AuthenticatedUser("u", Set.of("GUEST"), null, "");
        assertThrows(ForbiddenException.class, () -> guestService.resolveGuest(noSubject));
    }

    private static AuthenticatedUser actor(Long legacyGuestId, String keycloakId,
                                           String email, String firstName, String lastName) {
        return new AuthenticatedUser("user-1", Set.of("GUEST"), legacyGuestId, keycloakId,
                email, firstName, lastName);
    }

    private static Guest dbGuest(Long id, String keycloakId) {
        return new Guest(id, "John", "Doe", new EmailAddress("john@example.com"), null, keycloakId);
    }
}
